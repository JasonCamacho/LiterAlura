package com.literalura.literalura.repositorio;

import com.literalura.literalura.modelo.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorRepository extends JpaRepository<Autor, Long> {
}
