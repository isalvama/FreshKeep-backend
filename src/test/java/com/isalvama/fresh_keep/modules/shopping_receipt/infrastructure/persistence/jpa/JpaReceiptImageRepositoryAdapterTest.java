package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ReceiptImage;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.AssetId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ReceiptImageId;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.entity.JpaReceiptImageEntity;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.mapper.ReceiptImageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaReceiptImageRepositoryAdapter.class, ReceiptImageMapper.class})
class JpaReceiptImageRepositoryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JpaReceiptImageRepositoryAdapter adapter;

    @Autowired
    private JpaSpringDataReceiptImageRepository jpaSpringDataReceiptImageRepository;

    String receiptId = ReceiptImageId.create().toString();
    String receiptId2 = ReceiptImageId.create().toString();
    String assetId = "asset-id";
    String assetId2 = "asset-id2";
    String mimeType = "image/jpeg";


    @Nested
    class Save {

        @Test
        void save_shouldPersistReceiptImage() {

            ReceiptImageId receiptImageId = ReceiptImageId.create();

            ReceiptImage receiptImage = ReceiptImage.reconstitute(receiptImageId, AssetId.of(assetId), mimeType);

            adapter.save(receiptImage);
            jpaSpringDataReceiptImageRepository.flush();


            JpaReceiptImageEntity saved = jpaSpringDataReceiptImageRepository.findById(receiptImageId.value()).orElseThrow();

            assertEquals(receiptImageId.value(), saved.getId());
            assertEquals(assetId, saved.getAssetId());
            assertEquals(mimeType, saved.getMimeType());
            assertNotNull(saved.getCreatedAt());
            assertTrue(saved.getCreatedAt().isBefore(Instant.now().plus(1, ChronoUnit.MINUTES)));
            assertTrue(saved.getCreatedAt().isAfter(Instant.now().minus(1, ChronoUnit.MINUTES)));
        }
    }

    @Nested
    class FindById{
        @BeforeEach
        void setUp(){
            insertReceiptImage(UUID.fromString(receiptId), assetId, mimeType);
            insertReceiptImage(UUID.fromString(receiptId2), assetId2, mimeType);
        }

        @Test
        void shouldReturnDataOfReceiptImageWithMatchingId(){
            Optional<ReceiptImage> result = adapter.findById(ReceiptImageId.from(receiptId2));

            assertTrue(result.isPresent());

            ReceiptImage resultingReceiptImage = result.orElseThrow(RuntimeException::new);

            assertEquals(resultingReceiptImage.getId().toString(), receiptId2);
            assertEquals(resultingReceiptImage.getAssetId().toString(), assetId2);
            assertEquals(resultingReceiptImage.getMimeType(), mimeType);
        }
    }

    private void insertReceiptImage (UUID id, String assetId, String mimeType){
        jdbcTemplate.update(
                "INSERT INTO receipt_images (id, asset_id, mime_type) VALUES (?, ?, ?)",
                id, assetId, mimeType
        );
    }

}