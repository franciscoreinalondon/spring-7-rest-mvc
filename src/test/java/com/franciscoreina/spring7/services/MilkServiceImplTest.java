package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.request.milk.MilkCreateRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import com.franciscoreina.spring7.dto.request.milk.MilkUpdateRequest;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.mappers.MilkMapper;
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
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
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
    MilkRepository milkRepository;

    @Mock
    MilkMapper milkMapper;

    @InjectMocks
    MilkServiceImpl milkService;

    Category savedCategory = TestDataFactory.newSavedCategory();
    Milk milk;
    Milk savedMilk;
    MilkCreateRequest milkCreateRequest;
    MilkUpdateRequest milkUpdateRequest;
    MilkPatchRequest milkPatchRequest;
    MilkResponse milkResponse;

    @BeforeEach
    void setUp() {
        milk = TestDataFactory.newMilk(savedCategory);
        savedMilk = TestDataFactory.newSavedMilk(milk);
        milkCreateRequest = TestDataFactory.newMilkCreateRequest(milk);
        milkUpdateRequest = TestDataFactory.newMilkUpdateRequest(savedMilk);
        milkPatchRequest = TestDataFactory.newMilkPatchRequestWithName();
        milkResponse = TestDataFactory.newMilkResponse(savedMilk);
    }

    @Test
    void create_returnResponse_whenRequestValid() {
        // Arrange
        given(milkMapper.toEntity(milkCreateRequest)).willReturn(milk);
        given(milkRepository.save(milk)).willReturn(savedMilk);
        given(milkMapper.toResponse(savedMilk)).willReturn(milkResponse);

        // Act
        MilkResponse milkResponse = milkService.create(milkCreateRequest);

        // Assert
        assertThat(milkResponse).isSameAs(this.milkResponse);

        verify(milkMapper).toEntity(milkCreateRequest);
        verify(milkRepository).save(milk);
        verify(milkMapper).toResponse(savedMilk);
    }

    @Test
    void create_propagatesDataIntegrityException_whenRepoRejects() {
        // Arrange
        given(milkMapper.toEntity(milkCreateRequest)).willReturn(milk);
        given(milkRepository.save(milk)).willThrow(new DataIntegrityViolationException("Upc Duplicated"));

        // Act-Assert
        assertThatThrownBy(() -> milkService.create(milkCreateRequest))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(milkMapper).toEntity(milkCreateRequest);
        verify(milkRepository).save(milk);
    }

    @Test
    void getById_returnsResponse_whenMilkExists() {
        // Arrange
        UUID milkId = savedMilk.getId();
        given(milkRepository.findById(milkId)).willReturn(Optional.of(savedMilk));
        given(milkMapper.toResponse(savedMilk)).willReturn(milkResponse);

        // Act
        MilkResponse milkResponse = milkService.getById(milkId);

        // Assert
        assertThat(milkResponse).isSameAs(this.milkResponse);

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
        Milk savedMilk2 = TestDataFactory.newSavedMilk(TestDataFactory.newMilk(savedCategory));
        Pageable pageable = PageRequest.of(0, 20);

        given(milkRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(savedMilk, savedMilk2)));
        given(milkMapper.toResponse(savedMilk)).willReturn(TestDataFactory.newMilkResponse(savedMilk));
        given(milkMapper.toResponse(savedMilk2)).willReturn(TestDataFactory.newMilkResponse(savedMilk2));

        // Act
        Page<MilkResponse> milkResponseList = milkService.list(null, null, pageable);

        // Assert
        assertThat(milkResponseList).hasSize(2);
        assertThat(milkResponseList.getContent().getFirst().upc()).isEqualTo(savedMilk.getUpc());
        assertThat(milkResponseList.getContent().getLast().upc()).isEqualTo(savedMilk2.getUpc());

        verify(milkRepository).findAll(pageable);
        verify(milkMapper, times(1)).toResponse(savedMilk);
        verify(milkMapper, times(1)).toResponse(savedMilk2);
    }

    @Test
    void listByName_returnsList_whenMilksExist() {
        // Arrange
        savedMilk.setName("Skimmed name");
        Pageable pageable = PageRequest.of(0, 20);

        given(milkRepository.findAllByNameContainingIgnoreCase("Skimmed", pageable))
                .willReturn(new PageImpl<>(List.of(savedMilk)));
        given(milkMapper.toResponse(savedMilk)).willReturn(TestDataFactory.newMilkResponse(savedMilk));

        // Act
        Page<MilkResponse> milkResponseList = milkService.list("Skimmed", null, pageable);

        // Assert
        assertThat(milkResponseList).hasSize(1);
        assertThat(milkResponseList.getContent().getFirst().name()).isEqualTo(savedMilk.getName());

        verify(milkRepository).findAllByNameContainingIgnoreCase("Skimmed", pageable);
        verify(milkMapper, times(1)).toResponse(savedMilk);
    }

    @Test
    void listByType_returnsList_whenMilksExist() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);

        given(milkRepository.findAllByMilkType(MilkType.SEMI_SKIMMED, pageable))
                .willReturn(new PageImpl<>(List.of(savedMilk)));
        given(milkMapper.toResponse(savedMilk)).willReturn(TestDataFactory.newMilkResponse(savedMilk));

        // Act
        Page<MilkResponse> milkResponseList = milkService.list(null, MilkType.SEMI_SKIMMED, pageable);

        // Assert
        assertThat(milkResponseList.getContent()).hasSize(1);
        assertThat(milkResponseList.getContent().getFirst().milkType()).isEqualTo(savedMilk.getMilkType());

        verify(milkRepository).findAllByMilkType(MilkType.SEMI_SKIMMED, pageable);
        verify(milkMapper, times(1)).toResponse(savedMilk);
    }

    @Test
    void listByNameAndType_returnsList_whenMilksExist() {
        // Arrange Milk name
        Milk savedMilk2 = TestDataFactory.newSavedMilk(TestDataFactory.newMilk(savedCategory));
        Pageable pageable = PageRequest.of(0, 20);

        given(milkRepository.findAllByNameContainingIgnoreCaseAndMilkType("Milk name", MilkType.SEMI_SKIMMED, pageable))
                .willReturn(new PageImpl<>(List.of(savedMilk, savedMilk2)));
        given(milkMapper.toResponse(savedMilk)).willReturn(TestDataFactory.newMilkResponse(savedMilk));
        given(milkMapper.toResponse(savedMilk2)).willReturn(TestDataFactory.newMilkResponse(savedMilk2));

        // Act
        Page<MilkResponse> milkResponseList = milkService.list("Milk name", MilkType.SEMI_SKIMMED, pageable);

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
        Pageable pageable = PageRequest.of(0, 20);

        given(milkRepository.findAll(pageable)).willReturn(Page.empty());

        // Act
        Page<MilkResponse> milkResponseList = milkService.list(null, null, pageable);

        // Assert
        assertThat(milkResponseList.getContent()).isEmpty();

        verify(milkRepository).findAll(pageable);
        verifyNoInteractions(milkMapper);
    }

    @Test
    void update_updatesEntity_whenMilkExists() {
        // Arrange
        UUID milkId = savedMilk.getId();
        given(milkRepository.findById(milkId)).willReturn(Optional.of(savedMilk));

        // Act
        milkService.update(milkId, milkUpdateRequest);

        // Assert
        verify(milkRepository).findById(milkId);
        verify(milkMapper).updateEntity(savedMilk, milkUpdateRequest);
        verify(milkRepository).save(savedMilk);
    }

    @Test
    void update_throwsNotFound_whenMilkNotExists() {
        // Arrange
        given(milkRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // Act-Assert
        assertThatThrownBy(() -> milkService.update(UUID.randomUUID(), milkUpdateRequest))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(milkMapper);
    }

    @Test
    void update_propagatesDataIntegrityException_whenRepoRejects() {
        // Arrange
        given(milkRepository.findById(savedMilk.getId())).willReturn(Optional.of(savedMilk));
        given(milkRepository.save(savedMilk)).willThrow(new DataIntegrityViolationException("Upc Duplicated"));

        // Act-Assert
        assertThatThrownBy(() -> milkService.update(savedMilk.getId(), milkUpdateRequest))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(milkRepository).findById(savedMilk.getId());
        verify(milkMapper).updateEntity(savedMilk, milkUpdateRequest);
        verify(milkRepository).save(savedMilk);
    }

    @Test
    void patch_updatesOnlyProvidedFields_whenMilkExists() {
        // Arrange
        UUID milkId = savedMilk.getId();
        given(milkRepository.findById(milkId)).willReturn(Optional.of(savedMilk));

        // Act
        milkService.patch(milkId, milkPatchRequest);

        // Assert
        verify(milkRepository).findById(milkId);
        verify(milkMapper).patchEntity(savedMilk, milkPatchRequest);
        verify(milkRepository).save(savedMilk);
    }

    @Test
    void patch_throwsNotFound_whenMilkNotExists() {
        // Arrange
        given(milkRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // Act-Assert
        assertThatThrownBy(() -> milkService.patch(UUID.randomUUID(), milkPatchRequest))
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
        assertThatThrownBy(() -> milkService.patch(savedMilk.getId(), milkPatchRequest))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(milkRepository).findById(savedMilk.getId());
        verify(milkMapper).patchEntity(savedMilk, milkPatchRequest);
        verify(milkRepository).save(savedMilk);
    }

    @Test
    void delete_deletesMilk_whenMilkExists() {
        // Arrange
        UUID milkId = savedMilk.getId();
        given(milkRepository.findById(milkId)).willReturn(Optional.of(savedMilk));

        // Act
        milkService.delete(milkId);

        // Assert
        verify(milkRepository).findById(milkId);
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
