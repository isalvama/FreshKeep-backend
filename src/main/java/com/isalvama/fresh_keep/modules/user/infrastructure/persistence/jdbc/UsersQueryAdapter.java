package com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jdbc;

import com.isalvama.fresh_keep.modules.admin.application.port.out.UsersQueryPort;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.DailyMetricDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.RegisteredUserDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.RegisteredUsersPageDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.UserDetailsDto;
import com.isalvama.fresh_keep.shared.infrastructure.persistence.QueryAppender;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Component
@RequiredArgsConstructor
public class UsersQueryAdapter implements UsersQueryPort {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final QueryAppender queryAppender;

    private static final String GET_USER_REGISTRATIONS_QUERY = """
            SELECT CAST(u.created_at AS DATE) AS metric_date, COUNT(*) AS metric_count
            FROM users u
            WHERE 1 = 1
            """;

    private static final String FIND_REGISTERED_USERS_QUERY = """
                SELECT u.id, u.email, u.username, u.created_at AS registered_at,
                       a.last_log_in AS last_logged_at, COUNT(*) OVER() AS total_elements
                FROM users u
                JOIN accounts a ON a.id = u.account_id
                WHERE u.created_at >= :from
                  AND u.created_at < :toExclusive
                ORDER BY u.created_at DESC, u.id ASC
                LIMIT :limit OFFSET :offset
                """;

    private static final String FIND_BY_USER_ID_QUERY = """
                SELECT u.id, u.email, u.username, u.created_at AS registered_at,
                       a.last_log_in AS last_logged_at,
                       COALESCE(array_agg(r.role) FILTER (WHERE r.role IS NOT NULL), '{}') AS roles
                FROM users u
                JOIN accounts a ON a.id = u.account_id
                LEFT JOIN roles r ON r.account_id = a.id
                WHERE u.id = :userId
                GROUP BY u.id, u.email, u.username, u.created_at, a.last_log_in
                """;

    @Override
    public List<DailyMetricDto> getUserRegistrations(LocalDate from, LocalDate to) {
        MapSqlParameterSource parameters = queryAppender.parameters(from, to, 0, 0);
        String query = queryAppender.appendCommonFilters(GET_USER_REGISTRATIONS_QUERY, parameters, "u", "", false);
        return jdbcTemplate.query(query, parameters, queryAppender::mapMetric);
    }

    @Override
    public RegisteredUsersPageDto findRegisteredUsers(LocalDate from, LocalDate to, int offset, int limit) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("from", Timestamp.valueOf(from.atStartOfDay()))
                .addValue("toExclusive", Timestamp.valueOf(to.plusDays(1).atStartOfDay()))
                .addValue("limit", limit)
                .addValue("offset", offset);

        List<RegisteredUserRow> rows = jdbcTemplate.query(FIND_REGISTERED_USERS_QUERY, parameters, (rs, rowNum) -> new RegisteredUserRow(
                new RegisteredUserDto(
                        rs.getObject("id", java.util.UUID.class),
                        rs.getString("email"),
                        rs.getString("username"),
                        rs.getObject("registered_at", Timestamp.class).toInstant(),
                        rs.getObject("last_logged_at", Timestamp.class) == null
                                ? null
                                : rs.getObject("last_logged_at", Timestamp.class).toInstant()),
                rs.getLong("total_elements")));

        long totalElements = rows.isEmpty() ? 0 : rows.getFirst().totalElements();
        return new RegisteredUsersPageDto(rows.stream().map(RegisteredUserRow::user).toList(), totalElements);
    }

    @Override
    public Optional<UserDetailsDto> findByUserId(UUID userId) {
        List<UserDetailsDto> users = jdbcTemplate.query(FIND_BY_USER_ID_QUERY, Map.of("userId", userId), (rs, rowNum) -> new UserDetailsDto(
                rs.getObject("id", UUID.class),
                rs.getString("email"),
                rs.getString("username"),
                rs.getObject("registered_at", Timestamp.class).toInstant(),
                toInstant(rs.getObject("last_logged_at", Timestamp.class)),
                Arrays.asList((String[]) rs.getArray("roles").getArray()),
                List.of(),
                List.of()));

        if (users.isEmpty()) return Optional.empty();
        UserDetailsDto user = users.getFirst();

        List<UserDetailsDto.UserSpaceDto> spaces = jdbcTemplate.query("""
                SELECT s.id, s.name
                FROM spaces_participants sp
                JOIN spaces s ON s.id = sp.space_id
                WHERE sp.participant_id = :userId
                ORDER BY s.name ASC, s.id ASC
                """, Map.of("userId", userId), (rs, rowNum) ->
                new UserDetailsDto.UserSpaceDto(rs.getObject("id", UUID.class), rs.getString("name")));

        List<UserDetailsDto.UserReceiptDto> receipts = jdbcTemplate.query("""
                SELECT id, created_at, purchase_date, store_name
                FROM shopping_receipts
                WHERE creator_id = :userId
                ORDER BY created_at DESC, id ASC
                """, Map.of("userId", userId), (rs, rowNum) ->
                new UserDetailsDto.UserReceiptDto(
                        rs.getObject("id", UUID.class),
                        rs.getObject("created_at", Timestamp.class).toInstant(),
                        rs.getObject("purchase_date", Timestamp.class).toInstant().atZone(ZoneOffset.UTC).toLocalDate(),
                        rs.getString("store_name")));

        return Optional.of(new UserDetailsDto(
                user.id(), user.email(), user.username(), user.registeredAt(), user.lastLoggedAt(),
                user.roles(), spaces, receipts));
    }

    private record RegisteredUserRow(RegisteredUserDto user, long totalElements) {
    }

    private static java.time.Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

}
