package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.request.milk.MilkCreateRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkUpdateRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.mappers.MilkMapper;
import com.franciscoreina.spring7.repositories.CategoryRepository;
import com.franciscoreina.spring7.repositories.MilkRepository;
import com.franciscoreina.spring7.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
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

    Category newCategory;
    Category savedCategory;
    Milk newMilk;
    Milk savedMilk;
    MilkCreateRequest createRequest;
    MilkUpdateRequest updateRequest;
    MilkPatchRequest patchRequest;
    MilkResponse response;

    @BeforeEach
    void setUp() {
        newCategory = TestDataFactory.getNewCategory();
        savedCategory = TestDataFactory.getSavedCategory(newCategory);
        newMilk = TestDataFactory.getNewMilk(savedCategory);
        savedMilk = TestDataFactory.getSavedMilk(newMilk);
        createRequest = TestDataFactory.getMilkCreateRequest(newMilk);
        updateRequest = TestDataFactory.getMilkUpdateRequest(savedMilk);
        patchRequest = TestDataFactory.getMilkPatchRequestWithName();
        response = TestDataFactory.getMilkResponse(savedMilk);
    }

    @Test
    void create_returnResponse_whenRequestValid() {
        // Arrange
        given(categoryRepository.findAllById(any())).willReturn(List.of(savedCategory));//tbr
        given(milkMapper.toEntity(createRequest, Set.of(savedCategory))).willReturn(newMilk);
        given(milkRepository.save(newMilk)).willReturn(savedMilk);
        given(milkMapper.toResponse(savedMilk)).willReturn(response);

        // Act
        var milkResponse = milkService.create(createRequest);

        // Assert
        assertThat(milkResponse).isSameAs(this.response);

        verify(milkMapper).toEntity(createRequest, Set.of(savedCategory));
        verify(milkRepository).save(newMilk);
        verify(milkMapper).toResponse(savedMilk);
    }

    @Test
    void create_propagatesDataIntegrityException_whenRepoRejects() {
        // Arrange
        given(categoryRepository.findAllById(createRequest.categoryIds())).willReturn(List.of(savedCategory));
        given(milkMapper.toEntity(createRequest, Set.of(savedCategory))).willReturn(newMilk);
        given(milkRepository.save(newMilk)).willThrow(new DataIntegrityViolationException("Upc Duplicated"));

        // Act-Assert
        assertThatThrownBy(() -> milkService.create(createRequest))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(milkMapper).toEntity(createRequest, Set.of(savedCategory));
        verify(milkRepository).save(newMilk);
    }

    @Test
    void getById_returnsResponse_whenMilkExists() {
        // Arrange
        UUID milkId = savedMilk.getId();
        given(milkRepository.findById(milkId)).willReturn(Optional.of(savedMilk));
        given(milkMapper.toResponse(savedMilk)).willReturn(response);

        // Act
        var milkResponse = milkService.getById(milkId);

        // Assert
        assertThat(milkResponse).isSameAs(this.response);

        verify(milkRepository).findById(milkId);
        verify(milkMapper).toResponse(savedMilk);
    }

    @Test
    void getById_throwsNotFound_whenMilkNotExists() {
        // Arrange
        given(milkRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // Act-Assert
        assertThatThrownBy(() -> milkService.getById(UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);

        verify(milkRepository).findById(any(UUID.class));
        verifyNoInteractions(milkMapper);
    }

    @Test
    void list_returnsList_whenMilksExist() {
        // Arrange
        var savedMilk2 = TestDataFactory.getSavedMilk(TestDataFactory.getNewMilk(savedCategory));
        var pageable = PageRequest.of(0, 20);

        given(milkRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(savedMilk, savedMilk2)));
        given(milkMapper.toResponse(savedMilk)).willReturn(TestDataFactory.getMilkResponse(savedMilk));
        given(milkMapper.toResponse(savedMilk2)).willReturn(TestDataFactory.getMilkResponse(savedMilk2));

        // Act
       var milkResponseList = milkService.list(null, null, pageable);

        // Assert
        assertThat(milkResponseList).hasSize(2);
        assertThat(milkResponseList.getContent().getFirst().upc()).isEqualTo(savedMilk.getUpc());
        assertThat(milkResponseList.getContent().getLast().upc()).isEqualTo(savedMilk2.getUpc());

        verify(milkRepository).findAll(pageable);
        verify(milkMapper, times(1)).toResponse(savedMilk);
        verify(milkMapper, times(1)).toResponse(savedMilk2);
    }

//    @Test
//    void listByName_returnsList_whenMilksExist() {
//        // Arrange
//        savedMilk.setName("Skimmed name");
//        var pageable = PageRequest.of(0, 20);
//
//        given(milkRepository.findAllByNameContainingIgnoreCase("Skimmed", pageable))
//                .willReturn(new PageImpl<>(List.of(savedMilk)));
//        given(milkMapper.toResponse(savedMilk)).willReturn(TestDataFactory.getMilkResponse(savedMilk));
//
//        // Act
//        var milkResponseList = milkService.list("Skimmed", null, pageable);
//
//        // Assert
//        assertThat(milkResponseList).hasSize(1);
//        assertThat(milkResponseList.getContent().getFirst().name()).isEqualTo(savedMilk.getName());
//
//        verify(milkRepository).findAllByNameContainingIgnoreCase("Skimmed", pageable);
//        verify(milkMapper, times(1)).toResponse(savedMilk);
//    }

    @Test
    void listByType_returnsList_whenMilksExist() {
        // Arrange
        var pageable = PageRequest.of(0, 20);

        given(milkRepository.findAllByMilkType(MilkType.SEMI_SKIMMED, pageable))
                .willReturn(new PageImpl<>(List.of(savedMilk)));
        given(milkMapper.toResponse(savedMilk)).willReturn(TestDataFactory.getMilkResponse(savedMilk));

        // Act
        var milkResponseList = milkService.list(null, MilkType.SEMI_SKIMMED, pageable);

        // Assert
        assertThat(milkResponseList.getContent()).hasSize(1);
        assertThat(milkResponseList.getContent().getFirst().milkType()).isEqualTo(savedMilk.getMilkType());

        verify(milkRepository).findAllByMilkType(MilkType.SEMI_SKIMMED, pageable);
        verify(milkMapper, times(1)).toResponse(savedMilk);
    }

    @Test
    void listByNameAndType_returnsList_whenMilksExist() {
        // Arrange Milk name
        var savedMilk2 = TestDataFactory.getSavedMilk(TestDataFactory.getNewMilk(savedCategory));
        var pageable = PageRequest.of(0, 20);

        given(milkRepository.findAllByNameContainingIgnoreCaseAndMilkType("Milk name", MilkType.SEMI_SKIMMED, pageable))
                .willReturn(new PageImpl<>(List.of(savedMilk, savedMilk2)));
        given(milkMapper.toResponse(savedMilk)).willReturn(TestDataFactory.getMilkResponse(savedMilk));
        given(milkMapper.toResponse(savedMilk2)).willReturn(TestDataFactory.getMilkResponse(savedMilk2));

        // Act
        var milkResponseList = milkService.list("Milk name", MilkType.SEMI_SKIMMED, pageable);

        // Assert
        assertThat(milkResponseList).hasSize(2);
        assertThat(milkResponseList.getContent().getFirst().name()).isEqualTo(savedMilk.getName());
        assertThat(milkResponseList.getContent().getFirst().milkType()).isEqualTo(savedMilk.getMilkType());
        assertThat(milkResponseList.getContent().getLast().name()).isEqualTo(savedMilk2.getName());
        assertThat(milkResponseList.getContent().getLast().milkType()).isEqualTo(savedMilk2.getMilkType());

        verify(milkRepository).findAllByNameContainingIgnoreCaseAndMilkType("Milk name", MilkType.SEMI_SKIMMED, pageable);
        verify(milkMapper, times(1)).toResponse(savedMilk);
        verify(milkMapper, times(1)).toResponse(savedMilk2);
    }

    @Test
    void list_returnsEmptyList_whenNoMilks() {
        // Arrange
        var pageable = PageRequest.of(0, 20);

        given(milkRepository.findAll(pageable)).willReturn(Page.empty());

        // Act
        var milkResponseList = milkService.list(null, null, pageable);

        // Assert
        assertThat(milkResponseList.getContent()).isEmpty();

        verify(milkRepository).findAll(pageable);
        verifyNoInteractions(milkMapper);
    }

    @Test
    void update_updatesEntity_whenMilkExists() {
        // Arrange
        var savedMilkId = savedMilk.getId();
        given(milkRepository.findById(savedMilkId)).willReturn(Optional.of(savedMilk));

        // Act
        milkService.update(savedMilkId, updateRequest);

        // Assert
        verify(milkRepository).findById(savedMilkId);
        verify(milkMapper).updateEntity(savedMilk, updateRequest);
        verify(milkRepository).save(savedMilk);
    }

    @Test
    void update_throwsNotFound_whenMilkNotExists() {
        // Arrange
        given(milkRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // Act-Assert
        assertThatThrownBy(() -> milkService.update(UUID.randomUUID(), updateRequest))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(milkMapper);
    }

    @Test
    void update_propagatesDataIntegrityException_whenRepoRejects() {
        // Arrange
        given(milkRepository.findById(savedMilk.getId())).willReturn(Optional.of(savedMilk));
        given(milkRepository.save(savedMilk)).willThrow(new DataIntegrityViolationException("Upc Duplicated"));

        // Act-Assert
        assertThatThrownBy(() -> milkService.update(savedMilk.getId(), updateRequest))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(milkRepository).findById(savedMilk.getId());
        verify(milkMapper).updateEntity(savedMilk, updateRequest);
        verify(milkRepository).save(savedMilk);
    }

    @Test
    void patch_updatesOnlyProvidedFields_whenMilkExists() {
        // Arrange
        var savedMilkId = savedMilk.getId();
        given(milkRepository.findById(savedMilkId)).willReturn(Optional.of(savedMilk));

        // Act
        milkService.patch(savedMilkId, patchRequest);

        // Assert
        verify(milkRepository).findById(savedMilkId);
        verify(milkMapper).patchEntity(savedMilk, patchRequest);
        verify(milkRepository).save(savedMilk);
    }

    @Test
    void patch_throwsNotFound_whenMilkNotExists() {
        // Arrange
        given(milkRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // Act-Assert
        assertThatThrownBy(() -> milkService.patch(UUID.randomUUID(), patchRequest))
                .isInstanceOf(NotFoundException.class);

        verify(milkRepository).findById(any(UUID.class));
        verifyNoInteractions(milkMapper);
    }

    @Test
    void patch_propagatesDataIntegrityException_whenRepoRejects() {
        // Arrange
        given(milkRepository.findById(savedMilk.getId())).willReturn(Optional.of(savedMilk));
        given(milkRepository.save(savedMilk)).willThrow(new DataIntegrityViolationException("Upc Duplicated"));

        // Act-Assert
        assertThatThrownBy(() -> milkService.patch(savedMilk.getId(), patchRequest))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(milkRepository).findById(savedMilk.getId());
        verify(milkMapper).patchEntity(savedMilk, patchRequest);
        verify(milkRepository).save(savedMilk);
    }

    @Test
    void delete_deletesMilk_whenMilkExists() {
        // Arrange
        var savedMilkId = savedMilk.getId();
        given(milkRepository.findById(savedMilkId)).willReturn(Optional.of(savedMilk));

        // Act
        milkService.delete(savedMilkId);

        // Assert
        verify(milkRepository).findById(savedMilkId);
        verify(milkRepository).delete(savedMilk);
    }

    @Test
    void delete_throwsNotFound_whenMilkNotExists() {
        // Arrange
        given(milkRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // Act-Assert
        assertThatThrownBy(() -> milkService.delete(UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);

        verify(milkRepository).findById(any(UUID.class));
    }
}
