package org.yx.hoststack.center.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yx.hoststack.center.service.StorageFileService;

/**
 * 存储文件表
 *
 * @author lyc
 * @since 2025-02-05 16:00:24
 */
@RestController
@RequestMapping("/storageFile")
@RequiredArgsConstructor
public class StorageFileController {

    private StorageFileService storageFileService;


}