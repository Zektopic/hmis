# Performance Improvement Justification

We are unable to run a true runtime benchmark for this specific functionality since the EJB execution environment requires a container/database and we are prohibited from mocking.

However, the logical analysis clearly demonstrates the performance improvements:
The original method looped over the elements and iteratively called database facade commands (e.g. `getBillItemFacede().edit(b)` and `getPharmaceuticalBillItemFacade().edit(ph)`). This caused individual `EntityManager.merge(entity)` and generated numerous redundant transactions, resulting in an N+1 query pattern.

By restructuring the code, we instantiate lists (e.g., `billItemsToCreate`, `phItemsToCreate`, etc) and aggregate the entity modifications inside the loop. Once the loop has finished and all memory-side interactions and checks (like `addToStockWithoutHistory()` checking capacities) have been performed, we rely on `AbstractFacade.batchEdit` and `batchCreate`. These facade methods chunk the persist operations and control `EntityManager.flush()` and `EntityManager.clear()` over batch intervals.

This completely avoids repetitive transactional flushing and isolates the database transaction payload to optimized chunk boundaries.
