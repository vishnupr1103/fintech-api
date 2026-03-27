package com.fintech.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResponse<T> {
    private Collection<T> list;
    private int page;
    private int pageSize;
    private long totalElements;
    private int totalPage;
    private boolean lastPage;
}
