package nl.knaw.huc.api;


import nl.knaw.huc.core.Paginated;

public interface PaginatedResult extends Paginated {
  int getTotal();
}
