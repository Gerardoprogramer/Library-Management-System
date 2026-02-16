package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.modal.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GenreRepository extends JpaRepository<Genre, UUID> {

    boolean existsByCode(String code);

    List<Genre> findByActiveTrueOrderByDisplayOrderAsc();

    List<Genre> findByParentGenreIsNullAndActiveTrueOrderByDisplayOrderAsc();

    List<Genre> findByParentGenreIdAndActiveTrueOrderByDisplayOrderAsc(UUID parentId);

    long countByActiveTrue();

/*    @Query("select count(b) from book b where b.genre.id=:id")
    long countBooksByGenre(@Param("id") Long id);*/




}
