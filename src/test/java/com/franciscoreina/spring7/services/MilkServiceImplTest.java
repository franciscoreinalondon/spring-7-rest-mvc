package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.mappers.MilkMapper;
import com.franciscoreina.spring7.repositories.CategoryRepository;
import com.franciscoreina.spring7.repositories.MilkRepository;
import org.instancio.Instancio;
import org.instancio.Model;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class MilkServiceImplTest {

    @Mock
    CategoryRepository categoryRepository;
    @Mock
    MilkRepository milkRepository;
    @Mock
    MilkMapper milkMapper;
    @InjectMocks
    MilkServiceImpl milkService;

    // We use a model to ensure the categoryIds list size are constant in all tests
    private static final Model<MilkRequest> REQUEST_MODEL = Instancio.of(MilkRequest.class)
            .generate(field(MilkRequest::categoryIds), gen -> gen.collection().size(1))
            .toModel();

    // ---------------
    //    POSITIVE
    // ---------------

    @Nested
    class PositiveTests {

        @Test
        void create_shouldReturnResponse_whenRequestValid() {
            // Arrange
            var categories = Instancio.ofSet(Category.class).size(1).create();
            var request = Instancio.create(REQUEST_MODEL);
            var milk = Instancio.create(Milk.class);
            var expectedResponse = Instancio.create(MilkResponse.class);

            given(categoryRepository.findAllById(request.categoryIds())).willReturn(List.copyOf(categories));
            given(milkMapper.toEntity(request, categories)).willReturn(milk);
            given(milkRepository.save(milk)).willReturn(milk);
            given(milkMapper.toResponse(milk)).willReturn(expectedResponse);

            // Act
            var response = milkService.create(request);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(milkRepository).save(milk);
        }

        @Test
        void getById_shouldReturnResponse_whenExists() {
            // Arrange
            var milk = Instancio.create(Milk.class);
            var expectedResponse = Instancio.create(MilkResponse.class);

            given(milkRepository.findById(milk.getId())).willReturn(Optional.of(milk));
            given(milkMapper.toResponse(milk)).willReturn(expectedResponse);

            // Act
            var response = milkService.getById(milk.getId());

            // Assert
            assertThat(expectedResponse).isEqualTo(response);
            verify(milkRepository).findById(milk.getId());
        }

        @Test
        void list_shouldReturnPageOfResponse_whenNoFilter() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var milk1 = Instancio.create(Milk.class);
            var milk2 = Instancio.create(Milk.class);
            var expectedResponse1 = Instancio.create(MilkResponse.class);
            var expectedResponse2 = Instancio.create(MilkResponse.class);
            var expectedPage = new PageImpl<>(List.of(milk1, milk2));

            given(milkRepository.findAll(pageable)).willReturn(expectedPage);
            given(milkMapper.toResponse(milk1)).willReturn(expectedResponse1);
            given(milkMapper.toResponse(milk2)).willReturn(expectedResponse2);

            // Act
            var page = milkService.search(null, null, pageable);

            // Assert
            assertThat(page.getContent()).hasSize(2);
            verify(milkRepository).findAll(pageable);
        }

        @Test
        void update_shouldUpdateMilk_whenExists() {
            // Arrange
            var categories = Instancio.ofSet(Category.class).size(1).create();
            var request = Instancio.create(REQUEST_MODEL);
            var milk = Instancio.create(Milk.class);
            var expectedResponse = Instancio.create(MilkResponse.class);

            given(categoryRepository.findAllById(request.categoryIds())).willReturn(List.copyOf(categories));
            given(milkRepository.findById(milk.getId())).willReturn(Optional.of(milk));
            given(milkMapper.toResponse(milk)).willReturn(expectedResponse);

            // Act
            var response = milkService.update(milk.getId(), request);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(milkRepository).findById(milk.getId());
            verify(milkRepository).save(milk);
        }

        @Test
        void patch_shouldPatchMilk_whenPartialRequest() {
            // Arrange
            var milk = Instancio.create(Milk.class);
            var patch = Instancio.of(MilkPatchRequest.class).set(field(MilkPatchRequest::categoryIds), null).create();
            var expectedResponse = Instancio.create(MilkResponse.class);

            given(milkRepository.findById(milk.getId())).willReturn(Optional.of(milk));
            given(milkMapper.toResponse(milk)).willReturn(expectedResponse);

            // Act
            var response = milkService.patch(milk.getId(), patch);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(milkRepository).save(milk);
        }

        @Test
        void delete_shouldCallRepository_whenExists() {
            // Arrange
            var milk = Instancio.create(Milk.class);

            given(milkRepository.findById(milk.getId())).willReturn(Optional.of(milk));

            // Act
            milkService.delete(milk.getId());

            // Assert
            verify(milkRepository).delete(milk);
        }
    }

    // ---------------
    //    NEGATIVE
    // ---------------

    @Nested
    class NegativeTests {

        @Test
        void create_shouldThrowException_whenDataViolation() {
            // Arrange
            var categories = Instancio.ofSet(Category.class).size(1).create();
            var request = Instancio.create(REQUEST_MODEL);
            var milk = Instancio.create(Milk.class);

            given(categoryRepository.findAllById(request.categoryIds())).willReturn(List.copyOf(categories));
            given(milkMapper.toEntity(request, categories)).willReturn(milk);
            given(milkRepository.save(milk)).willThrow(new DataIntegrityViolationException("Duplicated"));

            // Act + Assert
            assertThatThrownBy(() -> milkService.create(request))
                    .isInstanceOf(DataIntegrityViolationException.class);

            verify(milkRepository).save(milk);
        }

        @Test
        void getById_shouldThrowException_whenNotFound() {
            // Arrange
            var id = UUID.randomUUID();

            given(milkRepository.findById(id)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkService.getById(id))
                    .isInstanceOf(NotFoundException.class);

            verify(milkRepository).findById(id);
        }

        @Test
        void update_shouldThrowException_whenCategoryMissing() {
            // Arrange
            var id = UUID.randomUUID();
            var request = Instancio.create(MilkRequest.class);
            var milk = Instancio.create(Milk.class);

            given(milkRepository.findById(id)).willReturn(Optional.of(milk));
            given(categoryRepository.findAllById(request.categoryIds())).willReturn(List.of());

            // Act + Assert
            assertThatThrownBy(() -> milkService.update(id, request))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void update_shouldThrowException_whenDataViolation() {
            // Arrange
            var categories = Instancio.ofSet(Category.class).size(1).create();
            var request = Instancio.create(REQUEST_MODEL);
            var milk = Instancio.create(Milk.class);

            given(categoryRepository.findAllById(request.categoryIds())).willReturn(List.copyOf(categories));
            given(milkRepository.findById(milk.getId())).willReturn(Optional.of(milk));
            given(milkRepository.save(milk)).willThrow(new DataIntegrityViolationException("Duplicated"));

            // Act + Assert
            assertThatThrownBy(() -> milkService.update(milk.getId(), request))
                    .isInstanceOf(DataIntegrityViolationException.class);

            verify(milkRepository).findById(milk.getId());
            verify(milkRepository).save(milk);
        }

        @Test
        void update_throwsNotFound_whenMilkNotExists() {
            // Arrange
            var request = Instancio.create(REQUEST_MODEL);
            var milk = Instancio.create(Milk.class);

            given(milkRepository.findById(milk.getId())).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkService.update(milk.getId(), request))
                    .isInstanceOf(NotFoundException.class);

            verifyNoInteractions(milkMapper);
        }

        @Test
        void patch_shouldThrowException_whenDataViolation() {
            // Arrange
            var categories = Instancio.ofSet(Category.class).size(1).create();
            var patch = Instancio.of(MilkPatchRequest.class).set(field(MilkPatchRequest::categoryIds), null).create();
            var milk = Instancio.create(Milk.class);

            given(milkRepository.findById(milk.getId())).willReturn(Optional.of(milk));
            given(milkRepository.save(milk)).willThrow(new DataIntegrityViolationException("Duplicated"));

            // Act + Assert
            assertThatThrownBy(() -> milkService.patch(milk.getId(), patch))
                    .isInstanceOf(DataIntegrityViolationException.class);

            verify(milkRepository).findById(milk.getId());
            verify(milkRepository).save(milk);
        }

        @Test
        void patch_shouldThrowException_whenNotFound() {
            // Arrange
            var id = UUID.randomUUID();
            var patch = Instancio.create(MilkPatchRequest.class);

            given(milkRepository.findById(id)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkService.patch(id, patch))
                    .isInstanceOf(NotFoundException.class);

            verify(milkRepository).findById(id);
        }

        @Test
        void delete_shouldThrowException_whenNotFound() {
            // Arrange
            var milkId = UUID.randomUUID();

            given(milkRepository.findById(milkId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkService.delete(milkId))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
