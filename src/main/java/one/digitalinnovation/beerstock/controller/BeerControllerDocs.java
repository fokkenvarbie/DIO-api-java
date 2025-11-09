package one.digitalinnovation.beerstock.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "Beer Management", description = "Operations for managing beer stock")
public interface BeerControllerDocs {

    @Operation(
            summary = "Create a new beer",
            description = "Registers a new beer in the system",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Beer successfully created",
                            content = @Content(schema = @Schema(implementation = BeerDTO.class))),
                    @ApiResponse(responseCode = "400", description = "Missing or invalid fields")
            }
    )
    BeerDTO createBeer(
            @Parameter(description = "Beer data to be created", required = true)
            BeerDTO beerDTO
    ) throws BeerAlreadyRegisteredException;

    @Operation(
            summary = "Find beer by name",
            description = "Returns a beer found by its name",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Beer found",
                            content = @Content(schema = @Schema(implementation = BeerDTO.class))),
                    @ApiResponse(responseCode = "404", description = "Beer not found")
            }
    )
    BeerDTO findByName(
            @Parameter(description = "Name of the beer to search", required = true)
            @PathVariable String name
    ) throws BeerNotFoundException;

    @Operation(
            summary = "List all beers",
            description = "Returns a list of all registered beers",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of all beers",
                            content = @Content(schema = @Schema(implementation = BeerDTO.class)))
            }
    )
    List<BeerDTO> listBeers();

    @Operation(
            summary = "Delete beer by ID",
            description = "Deletes a beer from the system by its ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Beer successfully deleted"),
                    @ApiResponse(responseCode = "404", description = "Beer not found")
            }
    )
    void deleteById(
            @Parameter(description = "ID of the beer to delete", required = true)
            @PathVariable Long id
    ) throws BeerNotFoundException;
}
