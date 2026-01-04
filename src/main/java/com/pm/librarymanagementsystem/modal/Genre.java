package com.pm.librarymanagementsystem.modal;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "El código de género es obligatorio.")
    private String code;

    @NotBlank(message = "El nombre del género es obligatorio.")
    private String name;

    @Size(max = 500, message = "La descripción no debe superar los 500 caracteres.")
    private String description;

    @Min(value = 0, message = "El orden de visualización no puede ser negativo.")
    private Integer displayOrder = 0;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne
    private Genre parentGenre;

    @OneToMany
    private List<Genre> subGenres = new ArrayList<Genre>();

/*    @OneToMany(mappedBy = "genre", cascade = CascadeType.PERSIST)
    private List<Book> books = new ArrayList<Book>();*/

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updateAt;


}
