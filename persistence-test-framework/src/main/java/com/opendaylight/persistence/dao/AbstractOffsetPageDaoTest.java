/**
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.opendaylight.persistence.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.opendaylight.persistence.DataStore;
import org.opendaylight.persistence.PersistenceException;
import org.opendaylight.persistence.dao.OffsetPageDao;
import org.opendaylight.persistence.util.common.type.page.OffsetPage;
import org.opendaylight.persistence.util.common.type.page.OffsetPageRequest;
import org.opendaylight.yangtools.concepts.Identifiable;

/**
 * Integration test for {@link OffsetPageDao} implementations.
 * 
 * @param <I> type of the identifiable object's id
 * @param <T> type of the identifiable object (object to store in the data store)
 * @param <F> type of the associated filter
 * @param <S> type of the associated sort attribute or sort key used to construct sort
 *            specifications
 * @param <C> type of the query's execution context; the context managed by the {@link DataStore}
 * @param <D> type of the DAO to test
 * @author Fabiel Zuniga
 * @author Nachiket Abhyankar
 */
public abstract class AbstractOffsetPageDaoTest<I extends Serializable, T extends Identifiable<I>, F, S, C, D extends OffsetPageDao<I, T, F, S, C>>
        extends AbstractDaoTest<I, T, F, S, C, D> {

    /*
     * NOTE: Convert should be called inside the query's execute method (In a Unit of Work).
     * Otherwise Session Exceptions will be launched for entities that use lazy loading.
     */

    /**
     * Creates a new DAO integration test.
     * 
     * @param dataStore data store
     */
    public AbstractOffsetPageDaoTest(@Nonnull DataStore<C> dataStore) {
        super(dataStore);
    }

    /**
     * @throws PersistenceException if any errors occur during execution
     */
    @Test
    public void testPagedFind() throws PersistenceException {
        List<SearchCase<T, F, S>> searchCases = getSearchCases();
        Assume.assumeNotNull(searchCases);

        for (final SearchCase<T, F, S> searchCase : searchCases) {
            // Persists the search space

            final Map<I, StoredObject<T>> persistedSearchSpace = store(searchCase.getSearchSpace());

            // Performs paged find

            // Try different page sizes

            int totalRecords = searchCase.getExpectedResult().size();

            for (int limit = 1; limit <= totalRecords; limit++) {
                int totalPages = totalRecords / limit;

                // handle extra non-full page at the end
                if (totalPages * limit < totalRecords) {
                    totalPages = totalPages + 1;
                }

                // Checks each page

                /*
                 * Search result will contain the aggregated records from all pages to compare at
                 * the end
                 */
                List<T> aggregatedResult = new ArrayList<T>(totalRecords);

                for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                    long offset = pageIndex * limit;
                    final OffsetPageRequest pageRequest = new OffsetPageRequest(offset, limit);

                    OffsetPage<T> resultPage = execute(new DaoQuery<OffsetPage<T>>() {
                        @Override
                        protected OffsetPage<T> execute(D dao, C context) throws PersistenceException {
                            return dao.find(searchCase.getFilter(), searchCase.getSort(), pageRequest,
                                    context);
                        }
                    });

                    // Compares result

                    Assert.assertEquals(getMessage(searchCase), totalPages, resultPage.getTotalPageCount());
                    Assert.assertEquals(getMessage(searchCase), totalRecords, resultPage.getTotalRecordCount());
                    if (pageIndex < totalPages - 1) {
                        Assert.assertEquals(getMessage(searchCase), limit, resultPage.getData().size());
                    }
                    else {
                        Assert.assertEquals(getMessage(searchCase), totalRecords - (pageIndex * limit), resultPage
                                .getData().size());
                    }

                    aggregatedResult.addAll(resultPage.getData());
                }

                assertSearch(searchCase.getExpectedResult(), aggregatedResult, persistedSearchSpace,
                        searchCase.isOrdered(),
                        getMessage(searchCase));
            }

            clear();
        }
    }
}
