package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import com.franciscoreina.spring7.exceptions.ConflictException;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class MilkServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private MilkRepository milkRepository;
    @Mock
    private MilkMapper milkMapper;
    @InjectMocks
    private MilkServiceImpl milkService;

    private static final UUID CATEGORY_ID_1 = UUID.randomUUID();
    private static final UUID CATEGORY_ID_2 = UUID.randomUUID();

    private static final Model<MilkRequest> REQUEST_MODEL = Instancio.of(MilkRequest.class)
            .set(field(MilkRequest::name), "Request milk")
            .set(field(MilkRequest::milkType), MilkType.WHOLE)
            .set(field(MilkRequest::upc), "UPC123")
            .set(field(MilkRequest::price), new BigDecimal("2.50"))
            .set(field(MilkRequest::stock), 10)
            .set(field(MilkRequest::categoryIds), new LinkedHashSet<>(Arrays.asList(CATEGORY_ID_1, CATEGORY_ID_2)))
            .toModel();

    private static final Model<MilkPatchRequest> PATCH_MODEL = Instancio.of(MilkPatchRequest.class)
            .set(field(MilkPatchRequest::name), "Patch milk")
            .set(field(MilkPatchRequest::milkType), MilkType.SKIMMED)
            .set(field(MilkPatchRequest::upc), "PATCH123")
            .set(field(MilkPatchRequest::price), new BigDecimal("3.75"))
            .set(field(MilkPatchRequest::stock), 20)
            .set(field(MilkPatchRequest::categoryIds), new LinkedHashSet<>(Arrays.asList(CATEGORY_ID_1, CATEGORY_ID_2)))
            .toModel();

    @Nested
    class CreateTests {

        @Test
        void create_shouldReturnResponse_whenRequestIsValid() {
            // Arrange
            var request = Instancio.create(REQUEST_MODEL);
            var category1 = Category.createCategory("Category 1");
            var category2 = Category.createCategory("Category 2");
            var categories = List.of(category1, category2);

            var milk = Milk.createMilk(
                    request.name(),
                    request.milkType(),
                    request.upc(),
                    request.price(),
                    request.stock(),
                    new HashSet<>(categories)
            );
            var expectedResponse = Instancio.create(MilkResponse.class);

            given(milkRepository.existsByUpcIgnoreCase(request.upc())).willReturn(false);
            given(categoryRepository.findAllById(request.categoryIds())).willReturn(categories);
            given(milkMapper.toEntity(request, new HashSet<>(categories))).willReturn(milk);
            given(milkRepository.save(milk)).willReturn(milk);
            given(milkMapper.toResponse(milk)).willReturn(expectedResponse);

            // Act
            var response = milkService.create(request);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(milkRepository).existsByUpcIgnoreCase(request.upc());
            verify(categoryRepository).findAllById(request.categoryIds());
            verify(milkMapper).toEntity(request, new HashSet<>(categories));
            verify(milkRepository).save(milk);
            verify(milkMapper).toResponse(milk);
        }

        @Test
        void create_shouldThrowConflictException_whenUpcAlreadyExists() {
            // Arrange
            var request = Instancio.create(REQUEST_MODEL);

            given(milkRepository.existsByUpcIgnoreCase(request.upc())).willReturn(true);

            // Act + Assert
            assertThatThrownBy(() -> milkService.create(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("Milk UPC already exists: " + request.upc());

            verify(milkRepository).existsByUpcIgnoreCase(request.upc());
            verifyNoInteractions(categoryRepository, milkMapper);
            verify(milkRepository, never()).save(any());
        }

        @Test
        void create_shouldThrowIllegalArgumentException_whenCategoryIdsAreEmpty() {
            // Arrange
            var request = new MilkRequest(
                    "Milk",
                    MilkType.WHOLE,
                    "UPC123",
                    new BigDecimal("2.50"),
                    10,
                    Set.of()
            );

            given(milkRepository.existsByUpcIgnoreCase(request.upc())).willReturn(false);

            // Act + Assert
            assertThatThrownBy(() -> milkService.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("At least one category is required");

            verify(milkRepository).existsByUpcIgnoreCase(request.upc());
            verify(categoryRepository, never()).findAllById(any());
            verifyNoInteractions(milkMapper);
        }

        @Test
        void create_shouldThrowNotFoundException_whenAnyCategoryDoesNotExist() {
            // Arrange
            var request = Instancio.create(REQUEST_MODEL);
            var category = Category.createCategory("Category ");

            given(milkRepository.existsByUpcIgnoreCase(request.upc())).willReturn(false);
            given(categoryRepository.findAllById(request.categoryIds())).willReturn(List.of(category));

            // Act + Assert
            assertThatThrownBy(() -> milkService.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("One or more categories not found");

            verify(milkRepository).existsByUpcIgnoreCase(request.upc());
            verify(categoryRepository).findAllById(request.categoryIds());
            verifyNoInteractions(milkMapper);
        }
    }

    @Nested
    class GetByIdTests {

        @Test
        void getById_shouldReturnResponse_whenMilkExists() {
            // Arrange
            var milkId = UUID.randomUUID();
            var milk = Milk.createMilk(
                    "Milk",
                    MilkType.WHOLE,
                    "UPC123",
                    new BigDecimal("2.50"),
                    10,
                    Set.of(Category.createCategory("Category"))
            );
            var expectedResponse = Instancio.create(MilkResponse.class);

            ReflectionTestUtils.setField(milk, "id", milkId);

            given(milkRepository.findById(milkId)).willReturn(Optional.of(milk));
            given(milkMapper.toResponse(milk)).willReturn(expectedResponse);

            // Act
            var response = milkService.getById(milkId);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(milkRepository).findById(milkId);
            verify(milkMapper).toResponse(milk);
        }

        @Test
        void getById_shouldThrowNotFoundException_whenMilkDoesNotExist() {
            // Arrange
            var milkId = UUID.randomUUID();

            given(milkRepository.findById(milkId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkService.getById(milkId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Milk not found: " + milkId);

            verify(milkRepository).findById(milkId);
            verify(milkMapper, never()).toResponse(any());
        }
    }

    @Nested
    class SearchTests {

        @Test
        void search_shouldReturnAllMilks_whenNoFiltersAreProvided() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var milk1 = Milk.createMilk(
                    "Milk 1", MilkType.WHOLE, "UPC111", new BigDecimal("1.50"), 10,
                    Set.of(Category.createCategory("Category 1"))
            );
            var milk2 = Milk.createMilk(
                    "Milk 2", MilkType.SKIMMED, "UPC222", new BigDecimal("2.50"), 20,
                    Set.of(Category.createCategory("Category 2"))
            );
            var response1 = Instancio.create(MilkResponse.class);
            var response2 = Instancio.create(MilkResponse.class);
            var page = new PageImpl<>(List.of(milk1, milk2), pageable, 2);

            given(milkRepository.findAll(pageable)).willReturn(page);
            given(milkMapper.toResponse(milk1)).willReturn(response1);
            given(milkMapper.toResponse(milk2)).willReturn(response2);

            // Act
            var result = milkService.search(null, null, pageable);

            // Assert
            assertThat(result.getContent()).containsExactly(response1, response2);
            verify(milkRepository).findAll(pageable);
        }

        @Test
        void search_shouldSearchByName_whenOnlyNameIsProvided() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var milk = Milk.createMilk(
                    "Whole Milk", MilkType.WHOLE, "UPC123", new BigDecimal("2.50"), 10,
                    Set.of(Category.createCategory("Category"))
            );
            var response = Instancio.create(MilkResponse.class);
            var page = new PageImpl<>(List.of(milk), pageable, 1);

            given(milkRepository.findAllByNameContainingIgnoreCase(milk.getName(), pageable)).willReturn(page);
            given(milkMapper.toResponse(milk)).willReturn(response);

            // Act
            var result = milkService.search(" Whole Milk ", null, pageable);

            // Assert
            assertThat(result.getContent()).containsExactly(response);
            verify(milkRepository).findAllByNameContainingIgnoreCase(milk.getName(), pageable);
        }

        @Test
        void search_shouldSearchByMilkType_whenOnlyMilkTypeIsProvided() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var milk = Milk.createMilk(
                    "Whole Milk", MilkType.WHOLE, "UPC123", new BigDecimal("2.50"), 10,
                    Set.of(Category.createCategory("Category"))
            );
            var response = Instancio.create(MilkResponse.class);
            var page = new PageImpl<>(List.of(milk), pageable, 1);

            given(milkRepository.findAllByMilkType(milk.getMilkType(), pageable)).willReturn(page);
            given(milkMapper.toResponse(milk)).willReturn(response);

            // Act
            var result = milkService.search(null, MilkType.WHOLE, pageable);

            // Assert
            assertThat(result.getContent()).containsExactly(response);
            verify(milkRepository).findAllByMilkType(milk.getMilkType(), pageable);
        }

        @Test
        void search_shouldSearchByNameAndMilkType_whenBothFiltersAreProvided() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var milk = Milk.createMilk(
                    "Whole Milk", MilkType.WHOLE, "UPC123", new BigDecimal("2.50"), 10,
                    Set.of(Category.createCategory("Category"))
            );
            var response = Instancio.create(MilkResponse.class);
            var page = new PageImpl<>(List.of(milk), pageable, 1);

            given(milkRepository.findAllByNameContainingIgnoreCaseAndMilkType(milk.getName(), milk.getMilkType(), pageable))
                    .willReturn(page);
            given(milkMapper.toResponse(milk)).willReturn(response);

            // Act
            var result = milkService.search(" Whole Milk ", MilkType.WHOLE, pageable);

            // Assert
            assertThat(result.getContent()).containsExactly(response);
            verify(milkRepository).findAllByNameContainingIgnoreCaseAndMilkType(milk.getName(), milk.getMilkType(), pageable);
        }

        @Test
        void search_shouldTreatBlankNameAsNull() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var page = new PageImpl<Milk>(List.of(), pageable, 0);

            given(milkRepository.findAll(pageable)).willReturn(page);

            // Act
            var result = milkService.search(" ", null, pageable);

            // Assert
            assertThat(result.getContent()).isEmpty();
            verify(milkRepository).findAll(pageable);
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void update_shouldReturnUpdatedResponse_whenRequestIsValid() {
            // Arrange
            var milkId = UUID.randomUUID();
            var request = Instancio.create(REQUEST_MODEL);
            var currentMilk = Milk.createMilk(
                    "Old Milk",
                    MilkType.SKIMMED,
                    "OLD123",
                    new BigDecimal("1.50"),
                    5,
                    Set.of(Category.createCategory("Old Category"))
            );
            var category1 = Category.createCategory("Category 1");
            var category2 = Category.createCategory("Category 2");
            var categories = List.of(category1, category2);
            var expectedResponse = Instancio.create(MilkResponse.class);

            ReflectionTestUtils.setField(currentMilk, "id", milkId);

            given(milkRepository.findById(milkId)).willReturn(Optional.of(currentMilk));
            given(milkRepository.findByUpcIgnoreCase(request.upc())).willReturn(Optional.empty());
            given(categoryRepository.findAllById(request.categoryIds())).willReturn(categories);
            given(milkMapper.toResponse(currentMilk)).willReturn(expectedResponse);

            // Act
            var response = milkService.update(milkId, request);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(milkRepository).findById(milkId);
            verify(milkRepository).findByUpcIgnoreCase(request.upc());
            verify(categoryRepository).findAllById(request.categoryIds());
            verify(milkMapper).updateEntity(currentMilk, request, new HashSet<>(categories));
            verify(milkMapper).toResponse(currentMilk);
            verify(milkRepository, never()).save(any());
        }

        @Test
        void update_shouldNotCheckUpcUniqueness_whenUpcDoesNotChange() {
            // Arrange
            var milkId = UUID.randomUUID();
            var request = new MilkRequest(
                    "Updated Milk",
                    MilkType.WHOLE,
                    "SAME123",
                    new BigDecimal("3.00"),
                    15,
                    new LinkedHashSet<>(Arrays.asList(CATEGORY_ID_1, CATEGORY_ID_2))
            );
            var currentMilk = Milk.createMilk(
                    "Old Milk",
                    MilkType.SKIMMED,
                    "SAME123",
                    new BigDecimal("1.50"),
                    5,
                    Set.of(Category.createCategory("Old Category"))
            );
            var category1 = Category.createCategory("Category 1");
            var category2 = Category.createCategory("Category 2");
            var categories = List.of(category1, category2);
            var expectedResponse = Instancio.create(MilkResponse.class);

            ReflectionTestUtils.setField(currentMilk, "id", milkId);

            given(milkRepository.findById(milkId)).willReturn(Optional.of(currentMilk));
            given(categoryRepository.findAllById(request.categoryIds())).willReturn(categories);
            given(milkMapper.toResponse(currentMilk)).willReturn(expectedResponse);

            // Act
            var response = milkService.update(milkId, request);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(milkMapper).updateEntity(currentMilk, request, new HashSet<>(categories));
            verify(milkRepository, never()).findByUpcIgnoreCase(any());
        }

        @Test
        void update_shouldThrowConflictException_whenUpcBelongsToAnotherMilk() {
            // Arrange
            var milkId = UUID.randomUUID();
            var existingMilkId = UUID.randomUUID();
            var request = Instancio.create(REQUEST_MODEL);
            var currentMilk = Milk.createMilk(
                    "Current Milk",
                    MilkType.WHOLE,
                    "CURRENT123",
                    new BigDecimal("2.50"),
                    10,
                    Set.of(Category.createCategory("Current Category"))
            );
            var existingMilk = Milk.createMilk(
                    "Existing Milk",
                    MilkType.SKIMMED,
                    request.upc(),
                    new BigDecimal("3.00"),
                    8,
                    Set.of(Category.createCategory("Existing Category"))
            );

            ReflectionTestUtils.setField(currentMilk, "id", milkId);
            ReflectionTestUtils.setField(existingMilk, "id", existingMilkId);

            given(milkRepository.findById(milkId)).willReturn(Optional.of(currentMilk));
            given(milkRepository.findByUpcIgnoreCase(request.upc())).willReturn(Optional.of(existingMilk));

            // Act + Assert
            assertThatThrownBy(() -> milkService.update(milkId, request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("Milk UPC already exists: " + request.upc());

            verify(milkRepository).findById(milkId);
            verify(milkRepository).findByUpcIgnoreCase(request.upc());
            verify(milkMapper, never()).updateEntity(any(), any(), any());
        }

        @Test
        void update_shouldThrowIllegalArgumentException_whenCategoryIdsAreEmpty() {
            // Arrange
            var milkId = UUID.randomUUID();
            var request = new MilkRequest(
                    "Milk",
                    MilkType.WHOLE,
                    "NEW123",
                    new BigDecimal("2.50"),
                    10,
                    Set.of()
            );
            var currentMilk = Milk.createMilk(
                    "Current Milk",
                    MilkType.SKIMMED,
                    "CURRENT123",
                    new BigDecimal("1.50"),
                    5,
                    Set.of(Category.createCategory("Current Category"))
            );

            ReflectionTestUtils.setField(currentMilk, "id", milkId);

            given(milkRepository.findById(milkId)).willReturn(Optional.of(currentMilk));
            given(milkRepository.findByUpcIgnoreCase(request.upc())).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkService.update(milkId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("At least one category is required");

            verify(milkRepository).findById(milkId);
            verify(milkRepository).findByUpcIgnoreCase(request.upc());
            verify(categoryRepository, never()).findAllById(any());
            verify(milkMapper, never()).updateEntity(any(), any(), any());
        }

        @Test
        void update_shouldThrowNotFoundException_whenAnyCategoryDoesNotExist() {
            // Arrange
            var milkId = UUID.randomUUID();
            var request = Instancio.create(REQUEST_MODEL);
            var currentMilk = Milk.createMilk(
                    "Current Milk",
                    MilkType.SKIMMED,
                    "CURRENT123",
                    new BigDecimal("1.50"),
                    5,
                    Set.of(Category.createCategory("Current Category"))
            );
            var category1 = Category.createCategory("Category 1");

            ReflectionTestUtils.setField(currentMilk, "id", milkId);

            given(milkRepository.findById(milkId)).willReturn(Optional.of(currentMilk));
            given(milkRepository.findByUpcIgnoreCase(request.upc())).willReturn(Optional.empty());
            given(categoryRepository.findAllById(request.categoryIds())).willReturn(List.of(category1));

            // Act + Assert
            assertThatThrownBy(() -> milkService.update(milkId, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("One or more categories not found");

            verify(milkRepository).findById(milkId);
            verify(milkRepository).findByUpcIgnoreCase(request.upc());
            verify(categoryRepository).findAllById(request.categoryIds());
            verify(milkMapper, never()).updateEntity(any(), any(), any());
        }

        @Test
        void update_shouldThrowNotFoundException_whenMilkDoesNotExist() {
            // Arrange
            var milkId = UUID.randomUUID();
            var request = Instancio.create(REQUEST_MODEL);

            given(milkRepository.findById(milkId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkService.update(milkId, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Milk not found: " + milkId);

            verify(milkRepository).findById(milkId);
            verifyNoMoreInteractions(milkRepository);
        }
    }

    @Nested
    class PatchTests {

        @Test
        void patch_shouldReturnPatchedResponse_whenRequestContainsChanges() {
            // Arrange
            var milkId = UUID.randomUUID();
            var patch = new MilkPatchRequest(
                    "Patched Milk",
                    MilkType.SKIMMED,
                    "PATCH123",
                    new BigDecimal("4.50"),
                    25,
                    new LinkedHashSet<>(Arrays.asList(CATEGORY_ID_1, CATEGORY_ID_2))
            );
            var currentMilk = Milk.createMilk(
                    "Current Milk",
                    MilkType.WHOLE,
                    "CURRENT123",
                    new BigDecimal("2.50"),
                    10,
                    Set.of(Category.createCategory("Current Category"))
            );
            var category1 = Category.createCategory("Category 1");
            var category2 = Category.createCategory("Category 2");
            var categories = List.of(category1, category2);
            var expectedResponse = Instancio.create(MilkResponse.class);

            ReflectionTestUtils.setField(currentMilk, "id", milkId);

            given(milkRepository.findById(milkId)).willReturn(Optional.of(currentMilk));
            given(milkRepository.findByUpcIgnoreCase(patch.upc())).willReturn(Optional.empty());
            given(categoryRepository.findAllById(patch.categoryIds())).willReturn(categories);
            given(milkMapper.toResponse(currentMilk)).willReturn(expectedResponse);

            // Act
            var response = milkService.patch(milkId, patch);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(milkRepository).findById(milkId);
            verify(milkRepository).findByUpcIgnoreCase(patch.upc());
            verify(categoryRepository).findAllById(patch.categoryIds());
            verify(milkMapper).patchEntity(currentMilk, patch, new HashSet<>(categories));
            verify(milkMapper).toResponse(currentMilk);
            verify(milkRepository, never()).save(any());
        }

        @Test
        void patch_shouldReturnCurrentResponse_whenPatchIsEmpty() {
            // Arrange
            var milkId = UUID.randomUUID();
            var patch = new MilkPatchRequest(null, null, null, null, null, null);
            var currentMilk = Milk.createMilk(
                    "Current Milk",
                    MilkType.WHOLE,
                    "CURRENT123",
                    new BigDecimal("2.50"),
                    10,
                    Set.of(Category.createCategory("Current Category"))
            );
            var expectedResponse = Instancio.create(MilkResponse.class);

            ReflectionTestUtils.setField(currentMilk, "id", milkId);

            given(milkRepository.findById(milkId)).willReturn(Optional.of(currentMilk));
            given(milkMapper.toResponse(currentMilk)).willReturn(expectedResponse);

            // Act
            var response = milkService.patch(milkId, patch);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(milkRepository).findById(milkId);
            verify(milkRepository, never()).findByUpcIgnoreCase(any());
            verify(categoryRepository, never()).findAllById(any());
            verify(milkMapper, never()).patchEntity(any(), any(), any());
            verify(milkMapper).toResponse(currentMilk);
        }

        @Test
        void patch_shouldNotCheckUpcUniqueness_whenUpcIsNull() {
            // Arrange
            var milkId = UUID.randomUUID();
            var patch = new MilkPatchRequest("Patched Milk", MilkType.SKIMMED, null, new BigDecimal("4.50"), 25, null);
            var currentMilk = Milk.createMilk(
                    "Current Milk",
                    MilkType.WHOLE,
                    "CURRENT123",
                    new BigDecimal("2.50"),
                    10,
                    Set.of(Category.createCategory("Current Category"))
            );
            var expectedResponse = Instancio.create(MilkResponse.class);

            ReflectionTestUtils.setField(currentMilk, "id", milkId);

            given(milkRepository.findById(milkId)).willReturn(Optional.of(currentMilk));
            given(milkMapper.toResponse(currentMilk)).willReturn(expectedResponse);

            // Act
            var response = milkService.patch(milkId, patch);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(milkRepository, never()).findByUpcIgnoreCase(any());
            verify(milkMapper).patchEntity(currentMilk, patch, null);
            verify(milkMapper).toResponse(currentMilk);
        }

        @Test
        void patch_shouldNotCheckUpcUniqueness_whenUpcDoesNotChange() {
            // Arrange
            var milkId = UUID.randomUUID();
            var patch = new MilkPatchRequest("Patched Milk", MilkType.SKIMMED, "SAME123", new BigDecimal("4.50"), 25, null);
            var currentMilk = Milk.createMilk(
                    "Current Milk",
                    MilkType.WHOLE,
                    "SAME123",
                    new BigDecimal("2.50"),
                    10,
                    Set.of(Category.createCategory("Current Category"))
            );
            var expectedResponse = Instancio.create(MilkResponse.class);

            ReflectionTestUtils.setField(currentMilk, "id", milkId);

            given(milkRepository.findById(milkId)).willReturn(Optional.of(currentMilk));
            given(milkMapper.toResponse(currentMilk)).willReturn(expectedResponse);

            // Act
            var response = milkService.patch(milkId, patch);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(milkRepository, never()).findByUpcIgnoreCase(any());
            verify(milkMapper).patchEntity(currentMilk, patch, null);
            verify(milkMapper).toResponse(currentMilk);
        }

        @Test
        void patch_shouldThrowConflictException_whenUpcBelongsToAnotherMilk() {
            // Arrange
            var milkId = UUID.randomUUID();
            var existingMilkId = UUID.randomUUID();
            var patch = new MilkPatchRequest(null, null, "PATCH123", null, null, null);
            var currentMilk = Milk.createMilk(
                    "Current Milk",
                    MilkType.WHOLE,
                    "CURRENT123",
                    new BigDecimal("2.50"),
                    10,
                    Set.of(Category.createCategory("Current Category"))
            );
            var existingMilk = Milk.createMilk(
                    "Existing Milk",
                    MilkType.SKIMMED,
                    patch.upc(),
                    new BigDecimal("3.50"),
                    12,
                    Set.of(Category.createCategory("Existing Category"))
            );

            ReflectionTestUtils.setField(currentMilk, "id", milkId);
            ReflectionTestUtils.setField(existingMilk, "id", existingMilkId);

            given(milkRepository.findById(milkId)).willReturn(Optional.of(currentMilk));
            given(milkRepository.findByUpcIgnoreCase(patch.upc())).willReturn(Optional.of(existingMilk));

            // Act + Assert
            assertThatThrownBy(() -> milkService.patch(milkId, patch))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("Milk UPC already exists: " + patch.upc());

            verify(milkRepository).findById(milkId);
            verify(milkRepository).findByUpcIgnoreCase(patch.upc());
            verify(milkMapper, never()).patchEntity(any(), any(), any());
        }

        @Test
        void patch_shouldThrowIllegalArgumentException_whenCategoryIdsAreEmpty() {
            // Arrange
            var milkId = UUID.randomUUID();
            var patch = new MilkPatchRequest(null, null, null, null, null, Set.of());
            var currentMilk = Milk.createMilk(
                    "Current Milk",
                    MilkType.WHOLE,
                    "CURRENT123",
                    new BigDecimal("2.50"),
                    10,
                    Set.of(Category.createCategory("Current Category"))
            );

            ReflectionTestUtils.setField(currentMilk, "id", milkId);

            given(milkRepository.findById(milkId)).willReturn(Optional.of(currentMilk));

            // Act + Assert
            assertThatThrownBy(() -> milkService.patch(milkId, patch))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("At least one category is required");

            verify(milkRepository).findById(milkId);
            verify(categoryRepository, never()).findAllById(any());
            verify(milkMapper, never()).patchEntity(any(), any(), any());
        }

        @Test
        void patch_shouldThrowNotFoundException_whenAnyCategoryDoesNotExist() {
            // Arrange
            var milkId = UUID.randomUUID();
            var patch = new MilkPatchRequest(null, null, null, null, null, new LinkedHashSet<>(Arrays.asList(CATEGORY_ID_1, CATEGORY_ID_2)));
            var currentMilk = Milk.createMilk(
                    "Current Milk",
                    MilkType.WHOLE,
                    "CURRENT123",
                    new BigDecimal("2.50"),
                    10,
                    Set.of(Category.createCategory("Current Category"))
            );
            var category1 = Category.createCategory("Category 1");

            ReflectionTestUtils.setField(currentMilk, "id", milkId);

            given(milkRepository.findById(milkId)).willReturn(Optional.of(currentMilk));
            given(categoryRepository.findAllById(patch.categoryIds())).willReturn(List.of(category1));

            // Act + Assert
            assertThatThrownBy(() -> milkService.patch(milkId, patch))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("One or more categories not found");

            verify(milkRepository).findById(milkId);
            verify(categoryRepository).findAllById(patch.categoryIds());
            verify(milkMapper, never()).patchEntity(any(), any(), any());
        }

        @Test
        void patch_shouldThrowNotFoundException_whenMilkDoesNotExist() {
            // Arrange
            var milkId = UUID.randomUUID();
            var patch = Instancio.create(PATCH_MODEL);

            given(milkRepository.findById(milkId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkService.patch(milkId, patch))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Milk not found: " + milkId);

            verify(milkRepository).findById(milkId);
            verifyNoMoreInteractions(milkRepository);
        }
    }

    @Nested
    class DeleteTests {

        @Test
        void delete_shouldCallRepository_whenMilkExists() {
            // Arrange
            var milkId = UUID.randomUUID();
            var milk = Milk.createMilk(
                    "Milk",
                    MilkType.WHOLE,
                    "UPC123",
                    new BigDecimal("2.50"),
                    10,
                    Set.of(Category.createCategory("Category"))
            );

            ReflectionTestUtils.setField(milk, "id", milkId);

            given(milkRepository.findById(milkId)).willReturn(Optional.of(milk));

            // Act
            milkService.delete(milkId);

            // Assert
            verify(milkRepository).findById(milkId);
            verify(milkRepository).delete(milk);
        }

        @Test
        void delete_shouldThrowNotFoundException_whenMilkDoesNotExist() {
            // Arrange
            var milkId = UUID.randomUUID();

            given(milkRepository.findById(milkId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkService.delete(milkId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Milk not found: " + milkId);

            verify(milkRepository).findById(milkId);
            verify(milkRepository, never()).delete(any());
        }
    }
}
