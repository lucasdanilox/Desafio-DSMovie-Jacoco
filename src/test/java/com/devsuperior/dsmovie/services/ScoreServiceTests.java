package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {

    @InjectMocks
    private ScoreService service;
    @Mock
    private UserService userService;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private ScoreRepository scoreRepository;


    private ScoreDTO scoreDTO;
    private UserEntity userEntity;
    private long existingMovieID, nonExistingMovieID;
    private MovieEntity movieEntity;
    private ScoreEntity scoreEntity;

    @BeforeEach
    void setUp() throws Exception {

        existingMovieID = 1L;
        nonExistingMovieID = 2L;

        scoreDTO = ScoreFactory.createScoreDTO();
        userEntity = UserFactory.createUserEntity();
        movieEntity = MovieFactory.createMovieEntity();
        scoreEntity = ScoreFactory.createScoreEntity();

        ScoreEntity score = new ScoreEntity();
        score.setMovie(movieEntity);
        score.setUser(userEntity);
        score.setValue(4.1);
        movieEntity.getScores().add(score);

        Mockito.when(userService.authenticated()).thenReturn(userEntity);
        Mockito.when(movieRepository.findById(existingMovieID)).thenReturn(Optional.of(movieEntity));
        Mockito.when(scoreRepository.saveAndFlush(Mockito.any())).thenReturn(scoreEntity);
        Mockito.when(movieRepository.save(Mockito.any())).thenReturn(movieEntity);

        Mockito.when(movieRepository.findById(nonExistingMovieID)).thenReturn(Optional.empty());

    }

    @Test
    public void saveScoreShouldReturnMovieDTO() {

        final MovieDTO result = service.saveScore(scoreDTO);

        Assertions.assertNotNull(result);


    }

    @Test
    public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {

        MovieEntity movie = MovieFactory.createMovieEntity();
        movie.setId(nonExistingMovieID);
        UserEntity user = UserFactory.createUserEntity();
        ScoreEntity score = new ScoreEntity();

        score.setMovie(movie);
        score.setUser(user);
        score.setValue(4.1);
        movie.getScores().add(score);

        scoreDTO = new ScoreDTO(score);

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            final MovieDTO result = service.saveScore(scoreDTO);
        });

    }
}
