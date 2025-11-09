package one.digitalinnovation.beerstock.controller;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.dto.QuantityDTO;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.exception.BeerStockExceededException;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
import one.digitalinnovation.beerstock.service.BeerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Collections;

import static one.digitalinnovation.beerstock.utils.JsonConvertionUtils.asJsonString;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class BeerControllerTest {

    private static final String BEER_API_URL_PATH = "/api/v1/beers";
    private static final long VALID_BEER_ID = 1L;
    private static final long INVALID_BEER_ID = 999L;
    private static final String BEER_API_SUBPATH_INCREMENT_URL = "/increment";
    private static final String BEER_API_SUBPATH_DECREMENT_URL = "/decrement";

    private MockMvc mockMvc;

    @Mock
    private BeerService beerService;

    @InjectMocks
    private BeerController beerController;
    
    // --- INÍCIO DA CORREÇÃO ---
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(beerController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setViewResolvers((viewName, locale) -> new MappingJackson2JsonView())
                .build();
    }
    // --- FIM DA CORREÇÃO ---

    @Test
    void whenPOSTIsCalledThenABeerIsCreated() throws Exception {
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        when(beerService.createBeer(beerDTO)).thenReturn(beerDTO);

        mockMvc.perform(post(BEER_API_URL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(beerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(beerDTO.getName())))
                .andExpect(jsonPath("$.brand", is(beerDTO.getBrand())))
                .andExpect(jsonPath("$.type", is(beerDTO.getType().toString())));
    }

    @Test
    void whenPOSTIsCalledWithoutRequiredFieldThenBadRequestIsReturned() throws Exception {
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        beerDTO.setBrand(null);

        mockMvc.perform(post(BEER_API_URL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(beerDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPOSTDuplicateBeerIsCalledThenConflictIsReturned() throws Exception {
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        when(beerService.createBeer(beerDTO)).thenThrow(BeerAlreadyRegisteredException.class);

        mockMvc.perform(post(BEER_API_URL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(beerDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    void whenGETByNameIsCalledWithValidNameThenOkIsReturned() throws Exception {
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        when(beerService.findByName(beerDTO.getName())).thenReturn(beerDTO);

        mockMvc.perform(get(BEER_API_URL_PATH + "/" + beerDTO.getName())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(beerDTO.getName())));
    }

    @Test
    void whenGETByNameIsCalledWithInvalidNameThenNotFoundIsReturned() throws Exception {
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        when(beerService.findByName(beerDTO.getName())).thenThrow(BeerNotFoundException.class);

        mockMvc.perform(get(BEER_API_URL_PATH + "/" + beerDTO.getName())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGETListIsCalledThenOkIsReturned() throws Exception {
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        when(beerService.listAll()).thenReturn(Collections.singletonList(beerDTO));

        mockMvc.perform(get(BEER_API_URL_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(beerDTO.getName())));
    }

    @Test
    void whenDELETEIsCalledWithValidIdThenNoContentIsReturned() throws Exception {
        doNothing().when(beerService).deleteById(VALID_BEER_ID);

        mockMvc.perform(delete(BEER_API_URL_PATH + "/" + VALID_BEER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void whenDELETEIsCalledWithInvalidIdThenNotFoundIsReturned() throws Exception {
        doThrow(BeerNotFoundException.class).when(beerService).deleteById(INVALID_BEER_ID);

        mockMvc.perform(delete(BEER_API_URL_PATH + "/" + INVALID_BEER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenPATCHIncrementIsCalledThenOkIsReturned() throws Exception {
        QuantityDTO quantityDTO = QuantityDTO.builder().quantity(5).build();
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        beerDTO.setQuantity(beerDTO.getQuantity() + quantityDTO.getQuantity());

        when(beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity())).thenReturn(beerDTO);

        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(quantityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(beerDTO.getQuantity())));
    }

    @Test
    void whenPATCHIncrementGreaterThanMaxThenBadRequestIsReturned() throws Exception {
        QuantityDTO quantityDTO = QuantityDTO.builder().quantity(100).build();
        when(beerService.increment(VALID_BEER_ID, quantityDTO.getQuantity()))
                .thenThrow(BeerStockExceededException.class);

        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(quantityDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPATCHIncrementInvalidIdThenNotFoundIsReturned() throws Exception {
        QuantityDTO quantityDTO = QuantityDTO.builder().quantity(5).build();
        when(beerService.increment(INVALID_BEER_ID, quantityDTO.getQuantity()))
                .thenThrow(BeerNotFoundException.class);

        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + INVALID_BEER_ID + BEER_API_SUBPATH_INCREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(quantityDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenPATCHDecrementIsCalledThenOkIsReturned() throws Exception {
        QuantityDTO quantityDTO = QuantityDTO.builder().quantity(5).build();
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        // É importante que a cerveja simulada (mock) tenha uma quantidade suficiente para decrementar.
        // O BeerDTOBuilder padrão deve definir uma quantidade inicial.
        beerDTO.setQuantity(beerDTO.getQuantity() - quantityDTO.getQuantity()); 

        when(beerService.decrement(VALID_BEER_ID, quantityDTO.getQuantity())).thenReturn(beerDTO);

        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(quantityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(beerDTO.getQuantity())));
    }

    @Test
    void whenPATCHDecrementLowerThanZeroThenBadRequestIsReturned() throws Exception {
        QuantityDTO quantityDTO = QuantityDTO.builder().quantity(100).build();
        when(beerService.decrement(VALID_BEER_ID, quantityDTO.getQuantity()))
                .thenThrow(BeerStockExceededException.class); // BeerStockExceededException é comumente usada para baixo estoque/abaixo de zero

        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + VALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(quantityDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPATCHDecrementInvalidIdThenNotFoundIsReturned() throws Exception {
        QuantityDTO quantityDTO = QuantityDTO.builder().quantity(5).build();
        lenient().when(beerService.decrement(INVALID_BEER_ID, quantityDTO.getQuantity()))
                .thenThrow(BeerNotFoundException.class);

        mockMvc.perform(patch(BEER_API_URL_PATH + "/" + INVALID_BEER_ID + BEER_API_SUBPATH_DECREMENT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(quantityDTO)))
                .andExpect(status().isNotFound());
    }
}