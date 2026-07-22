package com.divudi.ejb;

import com.divudi.core.entity.pharmacy.PharmaceuticalItemCategory;
import com.divudi.core.facade.PharmaceuticalItemCategoryFacade;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PharmacyBeanTest {

    private static class ExceptionMockPharmaceuticalItemCategoryFacade extends PharmaceuticalItemCategoryFacade {
        @Override
        public PharmaceuticalItemCategory findFirstByJpql(String jpql, Map<String, Object> parameters) {
            throw new RuntimeException("Database error simulation");
        }
    }

    @Test
    public void testGetPharmaceuticalCategoryByName_ExceptionInFind() {
        PharmacyBean bean = new PharmacyBean();
        ExceptionMockPharmaceuticalItemCategoryFacade mockFacade = new ExceptionMockPharmaceuticalItemCategoryFacade();
        bean.setPharmaceuticalItemCategoryFacade(mockFacade);

        PharmaceuticalItemCategory result = bean.getPharmaceuticalCategoryByName("TestCat", false);
        assertNull(result, "Expected null when findFirstByJpql throws an Exception");
    }
}
