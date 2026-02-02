package co.com.bancolombia.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PageRequest {
    private int page;
    private int size;
    private String sortBy;
    private SortDirection sortDirection;

    public long getOffset() {
        return (long) page * size;
    }
}
