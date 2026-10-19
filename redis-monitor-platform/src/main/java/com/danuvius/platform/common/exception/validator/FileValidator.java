package com.danuvius.platform.common.exception.validator;

import com.danuvius.platform.common.exception.ParameterCode;
import com.danuvius.platform.common.exception.ParameterException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 文件上传参数校验。
 * 原 ExcelValidator 的能力已并入本类，原类可保留或废弃，互不影响。
 */
public final class FileValidator {

    /** 默认文件大小上限：10MB */
    public static final long DEFAULT_MAX_SIZE = 10L * 1024 * 1024;

    /** Excel 扩展名（大小写不敏感） */
    private static final List<String> EXCEL_EXTENSIONS =
            Arrays.asList("xls", "xlsx");

    private FileValidator() {
    }

    /** 文件非空（含文件名） */
    public static void notEmpty(MultipartFile file, String fieldName) {
        if (file == null || file.isEmpty()) {
            throw new ParameterException(ParameterCode.PARAM_FILE_EMPTY,
                    "字段[" + fieldName + "]不能为空");
        }
    }

    /** 取扩展名（小写），无扩展名返回 "" */
    public static String extensionOf(MultipartFile file) {
        String fileName = file == null ? null : file.getOriginalFilename();
        if (StringUtils.isBlank(fileName)) {
            throw new ParameterException(ParameterCode.PARAM_FILE_NAME_ERROR,
                    "文件名为空或格式不正确");
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** 校验文件扩展名在白名单内（大小写不敏感） */
    public static void allowedExtension(MultipartFile file, String[] allowed, String fieldName) {
        notEmpty(file, fieldName);
        String ext = extensionOf(file);
        for (String item : allowed) {
            if (item.equalsIgnoreCase(ext)) {
                return;
            }
        }
        throw new ParameterException(ParameterCode.PARAM_FILE_EXTENSION_ERROR,
                "字段[" + fieldName + "]文件格式不正确，请重试");
    }

    /** 校验文件大小（字节） */
    public static void maxSize(MultipartFile file, long maxBytes, String fieldName) {
        notEmpty(file, fieldName);
        if (file.getSize() > maxBytes) {
            throw new ParameterException(ParameterCode.PARAM_FILE_SIZE_ERROR,
                    "字段[" + fieldName + "]文件大小不能超过" + (maxBytes / 1024 / 1024) + "MB");
        }
    }

    /** Excel 校验（兼容原 ExcelValidator.excelValidate 的语义） */
    public static void validExcel(MultipartFile file) {
        validExcel(file, "file");
    }

    /** Excel 校验（可指定字段名） */
    public static void validExcel(MultipartFile file, String fieldName) {
        notEmpty(file, fieldName);
        String ext = extensionOf(file);
        for (String item : EXCEL_EXTENSIONS) {
            if (item.equalsIgnoreCase(ext)) {
                return;
            }
        }
        throw new ParameterException(ParameterCode.PARAM_FILE_EXTENSION_ERROR,
                "excel的格式不正确，请重试");
    }
}