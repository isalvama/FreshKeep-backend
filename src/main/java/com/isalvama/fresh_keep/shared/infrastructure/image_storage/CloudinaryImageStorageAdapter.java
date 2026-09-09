package com.isalvama.fresh_keep.shared.infrastructure.image_storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ImageStoragePort;
import com.isalvama.fresh_keep.shared.infrastructure.exception.ImageRetrievalException;
import com.isalvama.fresh_keep.shared.infrastructure.exception.ImageStorageException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
public class CloudinaryImageStorageAdapter implements ImageStoragePort {
    private final RestClient restClient;
    private final Cloudinary cloudinary;

    public CloudinaryImageStorageAdapter(RestClient.Builder restClientBuilder, Cloudinary cloudinary) {
        this.restClient = restClientBuilder.build();
        this.cloudinary = cloudinary;
    }

    @Override
    public String upload(MultipartFile file, String folder) {
        if (file.isEmpty()) {
            throw new ImageStorageException("file to upload is empty.");
        }
        try {
            Map<?, ?> imageUploadedData = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "folder", folder,
                            "overwrite", false,
                            "type", "private"
                    )
            );

            return (String) imageUploadedData.get("public_id");
        } catch (IOException e) {
            throw new ImageStorageException("failed to upload image to Cloudinary.", e);
        }
    }

    @Override
    public String retrieveUrl(String assetId) {
        return cloudinary.url().type("private").signed(true).generate(assetId);
    }

    @Override
    public byte[] fetchImageBytes(String url) {
        try {
            return restClient.get().uri(url).retrieve().onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new ImageRetrievalException("Error downloading image: " + response.getStatusCode());
                    })
                    .body(byte[].class);
        } catch (Exception e) {
            throw new ImageStorageException(e.getMessage());
        }
    }
}
