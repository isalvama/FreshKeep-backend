package com.isalvama.fresh_keep.shared.infrastructure.image_storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.Url;
import com.isalvama.fresh_keep.shared.infrastructure.exception.ImageRetrievalException;
import com.isalvama.fresh_keep.shared.infrastructure.exception.ImageStorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryImageStorageAdapterTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private Url url;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

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

    @Test
    void retrieveUrl_returnsSignedPrivateUrlGeneratedByCloudinary() {
        String assetId = "shopping_receipts/receipts/abc123";
        String expectedUrl = "https://res.cloudinary.com/demo/image/private/s--sig--/v1/" + assetId;
        when(cloudinary.url()).thenReturn(url);
        when(url.type("private")).thenReturn(url);
        when(url.signed(true)).thenReturn(url);
        when(url.generate(assetId)).thenReturn(expectedUrl);

        String result = adapter.retrieveUrl(assetId);

        assertEquals(expectedUrl, result);
    }

    @Test
    void retrieveUrl_requestsASignedPrivateUrlForTheGivenAssetId() {
        String assetId = "shopping_receipts/receipts/abc123";
        when(cloudinary.url()).thenReturn(url);
        when(url.type(anyString())).thenReturn(url);
        when(url.signed(anyBoolean())).thenReturn(url);

        adapter.retrieveUrl(assetId);

        verify(url).type("private");
        verify(url).signed(true);
        verify(url).generate(assetId);
    }

    @Test
    @SuppressWarnings("unchecked")
    void fetchImageBytes_returnsBodyBytesOnSuccess() {
        String imageUrl = "https://res.cloudinary.com/demo/image/private/s--sig--/v1/shopping_receipts/receipts/abc123";
        byte[] expectedBytes = "fake-image-content".getBytes();
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(imageUrl)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(Predicate.class), any(RestClient.ResponseSpec.ErrorHandler.class))).thenReturn(responseSpec);
        when(responseSpec.body(byte[].class)).thenReturn(expectedBytes);

        byte[] result = adapter.fetchImageBytes(imageUrl);

        assertArrayEquals(expectedBytes, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void fetchImageBytes_requestsTheGivenUrl() {
        String imageUrl = "https://res.cloudinary.com/demo/image/private/s--sig--/v1/shopping_receipts/receipts/abc123";
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(Predicate.class), any(RestClient.ResponseSpec.ErrorHandler.class))).thenReturn(responseSpec);
        when(responseSpec.body(byte[].class)).thenReturn(new byte[0]);

        adapter.fetchImageBytes(imageUrl);

        verify(requestHeadersUriSpec).uri(imageUrl);
    }

    @Test
    @SuppressWarnings("unchecked")
    void fetchImageBytes_throwsImageStorageExceptionWhenBodyRetrievalFails() {
        String imageUrl = "https://res.cloudinary.com/demo/image/private/s--sig--/v1/shopping_receipts/receipts/abc123";
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(Predicate.class), any(RestClient.ResponseSpec.ErrorHandler.class))).thenReturn(responseSpec);
        when(responseSpec.body(byte[].class)).thenThrow(new ImageRetrievalException("Error downloading imagen: 404 NOT_FOUND"));

        Exception exception = assertThrows(ImageStorageException.class, () -> adapter.fetchImageBytes(imageUrl));

        assertTrue(exception.getMessage().contains("Error downloading imagen: 404 NOT_FOUND"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void fetchImageBytes_registersAnErrorHandlerThatThrowsImageRetrievalExceptionOnErrorStatus() throws IOException {
        String imageUrl = "https://res.cloudinary.com/demo/image/private/s--sig--/v1/shopping_receipts/receipts/abc123";
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(Predicate.class), any(RestClient.ResponseSpec.ErrorHandler.class))).thenReturn(responseSpec);
        when(responseSpec.body(byte[].class)).thenReturn(new byte[0]);

        adapter.fetchImageBytes(imageUrl);

        ArgumentCaptor<Predicate<HttpStatusCode>> predicateCaptor = ArgumentCaptor.forClass(Predicate.class);
        ArgumentCaptor<RestClient.ResponseSpec.ErrorHandler> handlerCaptor = ArgumentCaptor.forClass(RestClient.ResponseSpec.ErrorHandler.class);
        verify(responseSpec).onStatus(predicateCaptor.capture(), handlerCaptor.capture());

        assertTrue(predicateCaptor.getValue().test(HttpStatus.NOT_FOUND));
        assertFalse(predicateCaptor.getValue().test(HttpStatus.OK));

        HttpRequest request = mock(HttpRequest.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);

        Exception exception = assertThrows(ImageRetrievalException.class, () -> handlerCaptor.getValue().handle(request, response));
        assertTrue(exception.getMessage().contains("Error downloading imagen: 404 NOT_FOUND"));
    }
}
