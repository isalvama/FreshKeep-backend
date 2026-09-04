package com.isalvama.fresh_keep.shared.infrastructure.image_storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ImageStoragePort;
import com.isalvama.fresh_keep.shared.infrastructure.exception.ImageStorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CloudinaryImageStorageAdapter implements ImageStoragePort {
    private final Cloudinary cloudinary;

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
                            "overwrite", false
                    )
            );

            return (String) imageUploadedData.get("public_id");
        } catch (IOException e) {
            throw new ImageStorageException("failed to upload image to Cloudinary.", e);
        }
    }
}
