package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {

    @InjectMocks
    private MovieService service;

    @Mock
    private MovieRepository repository;


    private long existingID, nonExistingID, dependentID;

    private PageImpl<MovieEntity> page;
    private MovieDTO movieDTO;
    private String title;
    private MovieEntity movieEntity;


    @BeforeEach
    void setUp() throws Exception {

        title = "Test Movie";
        movieEntity = MovieFactory.createMovieEntity();
        page = new PageImpl<>(List.of(movieEntity));
        movieDTO = new MovieDTO(movieEntity);

        existingID = 1L;
        nonExistingID = 2L;
        dependentID = 3L;

        Mockito.when(repository.searchByTitle(any(), (Pageable) any())).thenReturn(page);
        Mockito.when(repository.findById(existingID)).thenReturn(Optional.of(movieEntity));
        Mockito.when(repository.findById(nonExistingID)).thenReturn(Optional.empty());
        Mockito.when(repository.save(any())).thenReturn(movieEntity);
        Mockito.when(repository.getReferenceById(existingID)).thenReturn(movieEntity);
        Mockito.when(repository.getReferenceById(nonExistingID)).thenThrow(EntityNotFoundException.class);
        Mockito.when(repository.existsById(existingID)).thenReturn(true);
        Mockito.when(repository.existsById(dependentID)).thenReturn(true);
        Mockito.when(repository.existsById(nonExistingID)).thenReturn(false);
        Mockito.doNothing().when(repository).deleteById(existingID);
        Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentID);


    }


    @Test
    public void findAllShouldReturnPagedMovieDTO() {

        Pageable pageable = PageRequest.of(0, 12);

        Page<MovieDTO> result = service.findAll(title, pageable);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getSize(), 1);
        Assertions.assertEquals(result.iterator().next().getTitle(), title);

    }

    @Test
    public void findByIdShouldReturnMovieDTOWhenIdExists() {

        MovieDTO result = service.findById(existingID);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), existingID);
        Assertions.assertEquals(result.getTitle(), title);

    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.findById(nonExistingID);
        });
    }

    @Test
    public void insertShouldReturnMovieDTO() {

        MovieDTO result = service.insert(movieDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), movieEntity.getId());
        Assertions.assertEquals(result.getTitle(), movieEntity.getTitle());

    }

    @Test
    public void updateShouldReturnMovieDTOWhenIdExists() {

        MovieDTO result = service.update(existingID, movieDTO);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getId(), movieEntity.getId());
        Assertions.assertEquals(result.getTitle(), movieEntity.getTitle());

    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.update(nonExistingID, movieDTO);
        });
    }

    @Test
    public void deleteShouldDoNothingWhenIdExists() {

        Assertions.assertDoesNotThrow(() -> {
            service.delete(existingID);
        });
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(nonExistingID);
        });

    }

    @Test
    public void deleteShouldThrowDatabaseExceptionWhenDependentId() {

        Assertions.assertThrows(DatabaseException.class, () -> {
            service.delete(dependentID);
        });
    }
}
