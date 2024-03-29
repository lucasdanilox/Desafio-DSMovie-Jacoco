package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.controllers.MovieController;
import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.MovieGenreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class MovieService {

    @Autowired
    private MovieRepository repository;

    @Transactional(readOnly = true)
    public Page<MovieDTO> findAll(String title, Pageable pageable) {
        Page<MovieEntity> result = repository.searchByTitle(title, pageable);
        return result.map(x -> new MovieDTO(x));
    }

    @Transactional(readOnly = true)
    public Page<MovieGenreDTO> findAllMovieGenre(String title, Pageable pageable) {
        Page<MovieEntity> result = repository.searchByTitle(title, pageable);
        return result.map(x -> new MovieGenreDTO(x)
                .add(linkTo(methodOn(MovieController.class).findAllV1(title, pageable)).withSelfRel())
                .add(linkTo(methodOn(MovieController.class).findByIdV1(x.getId())).withRel("Get movie by id")));
    }


    @Transactional(readOnly = true)
    public MovieDTO findById(Long id) {
        MovieEntity result = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado"));
        return new MovieDTO(result);
    }

    @Transactional(readOnly = true)
    public MovieGenreDTO findByIdMovieGenre(Long id) {
        MovieEntity result = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado"));
        MovieGenreDTO dto = new MovieGenreDTO(result).add(linkTo(methodOn(MovieController.class).findByIdV1(id)).withSelfRel())
                .add(linkTo(methodOn(MovieController.class).findAllV1(null, Pageable.unpaged())).withRel("All Movies"))
                .add(linkTo(methodOn(MovieController.class).update(id, null)).withRel("Update Movie"))
                .add(linkTo(methodOn(MovieController.class).delete(id)).withRel("Delete Movie"));

        return dto;
    }

    @Transactional
    public MovieDTO insert(MovieDTO dto) {
        MovieEntity entity = new MovieEntity();
        copyDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return new MovieDTO(entity);
    }

    @Transactional
    public MovieDTO update(Long id, MovieDTO dto) {
        try {
            MovieEntity entity = repository.getReferenceById(id);
            copyDtoToEntity(dto, entity);
            entity = repository.save(entity);
            return new MovieDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Recurso não encontrado");
        }
    }

    public void delete(Long id) {
        if (!repository.existsById(id))
            throw new ResourceNotFoundException("Recurso não encontrado");
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Falha de integridade referencial");
        }
    }

    private void copyDtoToEntity(MovieDTO dto, MovieEntity entity) {
        entity.setTitle(dto.getTitle());
        entity.setScore(dto.getScore());
        entity.setCount(dto.getCount());
        entity.setImage(dto.getImage());
    }
}