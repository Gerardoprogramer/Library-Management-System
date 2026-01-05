package com.pm.librarymanagementsystem.repository;

import com.pm.librarymanagementsystem.modal.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    boolean existsByCode(String code);

}
