package com.qlkhachsan.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Ho tro phan trang trong tang controller (danh sach da loc -> cat theo trang)
 * va gom chuoi query string giu lai bo loc khi chuyen trang.
 */
public final class Pages {

    private Pages() {
    }

    /** Chuan hoa so trang (1-based, khong vuot qua tong so trang). */
    public static int normalize(Integer page, int totalPages) {
        int p = page == null ? 1 : page;
        if (p < 1) {
            p = 1;
        }
        if (totalPages > 0 && p > totalPages) {
            p = totalPages;
        }
        return p;
    }

    /** Cat mot doan cua danh sach theo trang (page 1-based). */
    public static <T> List<T> slice(List<T> items, int page, int size) {
        if (items.isEmpty() || size <= 0) {
            return items;
        }
        int from = (page - 1) * size;
        if (from >= items.size()) {
            return List.of();
        }
        return items.subList(from, Math.min(from + size, items.size()));
    }

    /** Tong so trang khi chia danh sach gom {@code total} phan tu theo kich thuoc {@code size}. */
    public static int totalPages(int total, int size) {
        if (size <= 0 || total <= 0) {
            return 1;
        }
        return (total + size - 1) / size;
    }

    /**
     * Gom cac cap (key, value) thanh query string, bo qua gia tri rong.
     * Dung de giu bo loc tren link phan trang, vi du: "keyword=ks&amp;status=ACTIVE".
     */
    public static String queryString(String... keyValuePairs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i + 1 < keyValuePairs.length; i += 2) {
            String value = keyValuePairs[i + 1];
            if (value == null || value.isBlank()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(keyValuePairs[i]).append('=')
                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
