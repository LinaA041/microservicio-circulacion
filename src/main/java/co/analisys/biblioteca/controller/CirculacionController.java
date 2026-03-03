package co.analisys.biblioteca.controller;
import co.analisys.biblioteca.model.LibroId;
import co.analisys.biblioteca.model.Prestamo;
import co.analisys.biblioteca.model.PrestamoId;
import co.analisys.biblioteca.model.UsuarioId;
import co.analisys.biblioteca.service.CirculacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/circulacion")
@Tag(name = "Circulación", description = "Operaciones de préstamo y devolución de libros en la biblioteca")
public class CirculacionController {
    @Autowired
    private CirculacionService circulacionService;

    @Operation(
            summary = "Prestar un libro",
            description = "Registra el préstamo de un libro a un usuario. Verifica disponibilidad, actualiza el catálogo y envía notificación. Requiere rol ROLE_LIBRARIAN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libro prestado exitosamente"),
            @ApiResponse(responseCode = "400", description = "El libro no está disponible para préstamo"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado — se requiere ROLE_LIBRARIAN")
    })
    @PostMapping("/prestar")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public void prestarLibro(
            @Parameter(description = "ID del usuario que solicita el préstamo", required = true, example = "U001")
            @RequestParam String usuarioId,

            @Parameter(description = "ID del libro a prestar", required = true, example = "L001")
            @RequestParam String libroId
    ) {
        circulacionService.prestarLibro(new UsuarioId(usuarioId), new LibroId(libroId));
    }

    @Operation(
            summary = "Devolver un libro",
            description = "Registra la devolución de un préstamo activo. Actualiza el estado del préstamo, libera el libro en el catálogo y envía notificación. Requiere rol ROLE_LIBRARIAN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libro devuelto exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado — se requiere ROLE_LIBRARIAN"),
            @ApiResponse(responseCode = "404", description = "Préstamo no encontrado")
    })
    @PostMapping("/devolver")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public void devolverLibro(
            @Parameter(description = "ID del préstamo a devolver", required = true, example = "P001")
            @RequestParam String prestamoId
    )  {
        circulacionService.devolverLibro(new PrestamoId(prestamoId));
    }

    @Operation(
            summary = "Obtener todos los préstamos",
            description = "Retorna la lista completa de préstamos registrados en el sistema. Requiere rol ROLE_LIBRARIAN o ROLE_USER.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de préstamos obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @GetMapping("/prestamos")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'USER')")
    public List<Prestamo> obtenerTodosPrestamos() {
        return circulacionService.obtenerTodosPrestamos();
    }

    @Operation(
            summary = "Estado del servicio (público)",
            description = "Endpoint público que retorna el estado operativo del microservicio. No requiere autenticación."
    )
    @ApiResponse(responseCode = "200", description = "El servicio está funcionando correctamente")
    @GetMapping("/public/status")
    public String getPublicStatus() {
        return "El servicio de circulación está funcionando correctamente";
    }
}

