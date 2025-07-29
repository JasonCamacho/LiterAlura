package com.literalura.literalura.repositorio;

import com.literalura.literalura.modelo.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    List<Libro> findByIdioma(String idioma);
    List<Libro> findByAutorNombreContainingIgnoreCase(String nombre);
    List<Libro> findByAutorAnioNacimientoLessThanEqualAndAutorAnioFallecimientoGreaterThan(Integer anioNacimiento, Integer anioFallecimiento);
    List<Libro> findByAutorAnioNacimientoLessThanEqualAndAutorAnioFallecimientoIsNull(Integer anioNacimiento);
}