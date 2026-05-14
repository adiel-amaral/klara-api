package com.klaraapi.service;

import com.klaraapi.dto.*;
import com.klaraapi.entity.Bill;
import com.klaraapi.entity.Category;
import com.klaraapi.enums.BillStatus;
import com.klaraapi.enums.Recurrence;
import com.klaraapi.exception.BusinessException;
import com.klaraapi.exception.ResourceNotFoundException;
import com.klaraapi.repository.BillRepository;
import com.klaraapi.repository.CategoryRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    BillRepository billRepository;

    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    BillService billService;

    @Nested
    class Create {

        @Test
        void shouldReturnDTO_whenRequestHasNoCategory() {
            var request = new BillRequestDTO("Netflix", new BigDecimal("59.90"),
                    LocalDate.of(2026, 5, 15), null, Recurrence.MONTHLY, null);
            given(billRepository.save(any())).willAnswer(inv -> withId(inv.getArgument(0)));

            BillResponseDTO result = billService.create(request);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("Netflix");
            assertThat(result.amount()).isEqualByComparingTo("59.90");
            assertThat(result.category()).isNull();
        }

        @Test
        void shouldReturnDTO_withCategory_whenCategoryIdProvided() {
            var request = new BillRequestDTO("Netflix", new BigDecimal("59.90"),
                    LocalDate.of(2026, 5, 15), null, Recurrence.MONTHLY, 2L);
            given(categoryRepository.findById(2L)).willReturn(Optional.of(category()));
            given(billRepository.save(any())).willAnswer(inv -> withId(inv.getArgument(0)));

            BillResponseDTO result = billService.create(request);

            assertThat(result.category()).isNotNull();
            assertThat(result.category().name()).isEqualTo("Assinaturas");
        }

        @Test
        void shouldThrowResourceNotFoundException_whenCategoryNotFound() {
            var request = new BillRequestDTO("Netflix", new BigDecimal("59.90"),
                    LocalDate.of(2026, 5, 15), null, Recurrence.MONTHLY, 99L);
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> billService.create(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    class Update {

        @Test
        void shouldReturnUpdatedDTO_whenBillExists() {
            var bill = bill("Antiga", BillStatus.PENDING);
            var request = new BillUpdateRequestDTO("Netflix", new BigDecimal("59.90"),
                    LocalDate.of(2026, 5, 15), null, Recurrence.MONTHLY, null);
            given(billRepository.findById(1L)).willReturn(Optional.of(bill));
            given(billRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            BillResponseDTO result = billService.update(1L, request);

            assertThat(result.name()).isEqualTo("Netflix");
        }

        @Test
        void shouldThrowResourceNotFoundException_whenBillNotFound() {
            var request = new BillUpdateRequestDTO("Netflix", new BigDecimal("59.90"),
                    LocalDate.of(2026, 5, 15), null, Recurrence.MONTHLY, null);
            given(billRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> billService.update(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        void shouldClearCategory_whenCategoryIdIsNull() {
            var bill = bill("Netflix", BillStatus.PENDING);
            bill.setCategory(category());
            var request = new BillUpdateRequestDTO("Netflix", new BigDecimal("59.90"),
                    LocalDate.of(2026, 5, 15), null, Recurrence.MONTHLY, null);
            given(billRepository.findById(1L)).willReturn(Optional.of(bill));
            given(billRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            BillResponseDTO result = billService.update(1L, request);

            assertThat(result.category()).isNull();
        }
    }

    @Nested
    class UpdateStatus {

        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource({"PENDING, PAID", "PENDING, CANCELLED", "OVERDUE, PAID", "OVERDUE, CANCELLED"})
        void shouldTransition_whenTransitionIsValid(BillStatus from, BillStatus to) {
            var bill = bill("Netflix", from);
            given(billRepository.findById(1L)).willReturn(Optional.of(bill));
            given(billRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            BillResponseDTO result = billService.updateStatus(1L, new BillStatusUpdateRequestDTO(to));

            assertThat(result.status()).isEqualTo(to);
        }

        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource({"PAID, CANCELLED", "PAID, PENDING", "CANCELLED, PAID", "CANCELLED, PENDING"})
        void shouldThrowBusinessException_whenTransitionIsInvalid(BillStatus from, BillStatus to) {
            var bill = bill("Netflix", from);
            var request = new BillStatusUpdateRequestDTO(to);
            given(billRepository.findById(1L)).willReturn(Optional.of(bill));

            assertThatThrownBy(() -> billService.updateStatus(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot transition from");
        }

        @Test
        void shouldThrowResourceNotFoundException_whenBillNotFound() {
            var request = new BillStatusUpdateRequestDTO(BillStatus.PAID);
            given(billRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> billService.updateStatus(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    class Delete {

        @Test
        void shouldDelete_whenBillExists() {
            given(billRepository.existsById(1L)).willReturn(true);

            billService.delete(1L);

            then(billRepository).should().deleteById(1L);
        }

        @Test
        void shouldThrowResourceNotFoundException_whenBillNotFound() {
            given(billRepository.existsById(99L)).willReturn(false);

            assertThatThrownBy(() -> billService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            then(billRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldReturnPage_whenFilterIsValid() {
            var page = new PageImpl<>(List.of(bill("Netflix", BillStatus.PENDING)));
            given(billRepository.findAll(ArgumentMatchers.<Specification<Bill>>any(), any(Pageable.class))).willReturn(page);

            var filter = new BillFilter(null, null, null, null, null);
            var result = billService.findAll(filter, 0, 10, "dueDate", "asc");

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().name()).isEqualTo("Netflix");
        }

        @Test
        void shouldThrowBusinessException_whenSortFieldIsInvalid() {
            var filter = new BillFilter(null, null, null, null, null);

            assertThatThrownBy(() -> billService.findAll(filter, 0, 10, "invalid", "asc"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Invalid sort field");
        }
    }

    private Bill withId(Bill bill) {
        bill.setId(1L);
        return bill;
    }

    private Bill bill(String name, BillStatus status) {
        var bill = new Bill();
        bill.setId(1L);
        bill.setName(name);
        bill.setAmount(new BigDecimal("100.00"));
        bill.setDueDate(LocalDate.of(2026, 5, 10));
        bill.setRecurrence(Recurrence.MONTHLY);
        bill.setStatus(status);
        return bill;
    }

    private Category category() {
        var c = new Category();
        c.setId(2L);
        c.setName("Assinaturas");
        return c;
    }
}
