package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStoragePort {

    String upload (MultipartFile file, String folder);

    String retrieveUrl(String assetId);

    byte[] fetchImageBytes(String url);
}
