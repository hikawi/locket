package dev.frilly.locket.dto.res;

import java.util.List;

/**
 * A response record for returning a paginated list of items.
 *
 * @param total      the total elements
 * @param totalPages the total pages of elements
 * @param page       the current page
 * @param perPage    the current specified per page
 * @param results    the results list
 * @param <T>        the type of the element
 */
public record PaginatedResponse<T>(long total, int totalPages, int page,
                                   int perPage, List<T> results) {

}
