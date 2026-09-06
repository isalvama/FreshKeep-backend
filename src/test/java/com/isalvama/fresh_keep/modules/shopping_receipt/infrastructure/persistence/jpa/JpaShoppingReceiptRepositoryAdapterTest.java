package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceipt;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ReceiptImageId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity.JpaShoppingReceiptEntity;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.mapper.ShoppingReceiptMapper;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaShoppingReceiptRepositoryAdapter.class, ShoppingReceiptMapper.class})
class JpaShoppingReceiptRepositoryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private JpaShoppingReceiptRepositoryAdapter adapter;

    @Autowired
    private JpaSpringDataShoppingReceiptRepository jpaRepository;

    @Autowired
    private Clock clock;

    private final String id = ShoppingReceiptId.create().toString();
    private final String creatorId = UserId.create().toString();
    private final String spaceId = SpaceId.create().toString();
    private final String receiptImageId = ReceiptImageId.create().toString();
    private final LocalDate purchaseDate = LocalDate.of(2026, 9, 6);
    private final String storeName = "Store Name";


    @Nested
    class Save{

        @Test
        void shouldSaveShoppingReceiptSuccessfullyAndUpdateCreatedAt(){
            adapter.save(ShoppingReceipt.reconstitute(ShoppingReceiptId.from(id), UserId.from(creatorId), SpaceId.from(spaceId), ReceiptImageId.from(receiptImageId), purchaseDate, storeName, clock));

            Optional<JpaShoppingReceiptEntity> result = jpaRepository.findById(UUID.fromString(id));
            jpaRepository.flush();

            assertTrue(result.isPresent());
            JpaShoppingReceiptEntity resultingEntity = result.orElseThrow(RuntimeException::new);
            assertEquals(resultingEntity.getId().toString(), id);
            assertEquals(resultingEntity.getCreatorId().toString(), creatorId);
            assertEquals(resultingEntity.getSpaceId().toString(), spaceId);
            assertEquals(resultingEntity.getReceiptImageId().toString(), receiptImageId);
            assertEquals(resultingEntity.getPurchaseDate().atZone(ZoneOffset.UTC).toLocalDate(), purchaseDate);
            assertEquals(resultingEntity.getStoreName(), storeName);
            assertTrue(resultingEntity.getCreatedAt().isBefore(Instant.now().plus(1, ChronoUnit.MINUTES)));
            assertTrue(resultingEntity.getCreatedAt().isAfter(Instant.now().minus(1, ChronoUnit.MINUTES)));
        }
    }
}