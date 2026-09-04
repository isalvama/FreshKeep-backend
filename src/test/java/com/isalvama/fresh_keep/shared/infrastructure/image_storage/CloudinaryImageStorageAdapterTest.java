package com.isalvama.fresh_keep.shared.infrastructure.image_storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.isalvama.fresh_keep.shared.infrastructure.exception.ImageStorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryImageStorageAdapterTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryImageStorageAdapter adapter;

    private final MultipartFile file = new MockMultipartFile("file", "receipt.jpg", "image/jpeg", "fake-image-content".getBytes());

    @Test
    void upload_throwsImageStorageExceptionWhenFileIsEmpty() {
        MultipartFile emptyFile = new MockMultipartFile("file", "receipt.jpg", "image/jpeg", new byte[0]);

        Exception exception = assertThrows(ImageStorageException.class, () -> adapter.upload(emptyFile, "shopping_receipts/receipts"));

        assertTrue(exception.getMessage().contains("empty"));
        verifyNoInteractions(cloudinary);
    }

    @Test
    void upload_returnsPublicIdOnSuccess() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(Map.of("public_id", "shopping_receipts/receipts/abc123"));

        String result = adapter.upload(file, "shopping_receipts/receipts");

        assertEquals("shopping_receipts/receipts/abc123", result);
    }

    @Test
    void upload_sendsTheFileBytesAndExpectedUploadOptions() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(Map.of("public_id", "some-id"));

        adapter.upload(file, "shopping_receipts/receipts");

        ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
        ArgumentCaptor<Map> optionsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(bytesCaptor.capture(), optionsCaptor.capture());

        assertArrayEquals(file.getBytes(), bytesCaptor.getValue());
        assertEquals("image", optionsCaptor.getValue().get("resource_type"));
        assertEquals("shopping_receipts/receipts", optionsCaptor.getValue().get("folder"));
        assertEquals(false, optionsCaptor.getValue().get("overwrite"));
    }

    @Test
    void upload_throwsImageStorageExceptionWhenCloudinaryThrowsIOException() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        IOException original = new IOException("network error");
        when(uploader.upload(any(byte[].class), any(Map.class))).thenThrow(original);

        Exception exception = assertThrows(ImageStorageException.class, () -> adapter.upload(file, "shopping_receipts/receipts"));

        assertTrue(exception.getMessage().contains("failed to upload"));
        assertSame(original, exception.getCause());
    }
}
