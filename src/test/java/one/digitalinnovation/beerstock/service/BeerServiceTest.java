package one.digitalinnovation.beerstock.service;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.exception.BeerStockExceededException;
import one.digitalinnovation.beerstock.mapper.BeerMapper;
import one.digitalinnovation.beerstock.repository.BeerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings; // Adicionar esta
import org.mockito.quality.Strictness; // Adicionar esta

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any; // Usamos o Mockito.any() explicitamente

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // <--- CORREÇÃO 1: Evita UnnecessaryStubbingException
public class BeerServiceTest {

    private static final long INVALID_BEER_ID = 999L;

    @Mock
    private BeerRepository beerRepository;

    @Mock
    private BeerMapper beerMapper;

    @InjectMocks
    private BeerService beerService;

    // --- Método Auxiliar para criar Beer com nome ---
    private Beer createBeerFromDTO(BeerDTO dto) {
        // CORREÇÃO 2: Garante que a entidade tenha o nome para evitar findByName(null)
        return Beer.builder()
                .id(dto.getId())
                .name(dto.getName())
                .brand(dto.getBrand())
                .max(dto.getMax())
                .quantity(dto.getQuantity())
                .type(dto.getType())
                .build();
    }
    // ----------------------------------------------

    @Test
    void whenBeerInformedThenItShouldBeCreated() throws BeerAlreadyRegisteredException {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedSavedBeer = createBeerFromDTO(expectedBeerDTO);

        when(beerMapper.toModel(expectedBeerDTO)).thenReturn(expectedSavedBeer);
        when(beerMapper.toDTO(expectedSavedBeer)).thenReturn(expectedBeerDTO);
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.empty());
        when(beerRepository.save(expectedSavedBeer)).thenReturn(expectedSavedBeer);

        BeerDTO createdBeerDTO = beerService.createBeer(expectedBeerDTO);

        assertThat(createdBeerDTO.getId(), is(equalTo(expectedBeerDTO.getId())));
        assertThat(createdBeerDTO.getName(), is(equalTo(expectedBeerDTO.getName())));
        assertThat(createdBeerDTO.getQuantity(), is(equalTo(expectedBeerDTO.getQuantity())));
    }

    @Test
    void whenAlreadyRegisteredBeerInformedThenThrowException() {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        // Usa o createBeerFromDTO para garantir que o nome não é null
        Beer duplicatedBeer = createBeerFromDTO(expectedBeerDTO);
        
        when(beerMapper.toModel(expectedBeerDTO)).thenReturn(duplicatedBeer);
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.of(duplicatedBeer));

        assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(expectedBeerDTO));
    }

    @Test
    void whenValidBeerNameIsGivenThenReturnBeer() throws BeerNotFoundException {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        // CORREÇÃO 2: Garante que a entidade tenha o nome
        Beer expectedBeer = createBeerFromDTO(expectedBeerDTO);

        when(beerMapper.toDTO(expectedBeer)).thenReturn(expectedBeerDTO);
        // CORREÇÃO 2: findByName agora busca pelo nome real, não null
        when(beerRepository.findByName(expectedBeer.getName())).thenReturn(Optional.of(expectedBeer));

        BeerDTO foundBeerDTO = beerService.findByName(expectedBeerDTO.getName());

        assertThat(foundBeerDTO, is(equalTo(expectedBeerDTO)));
    }

    @Test
    void whenNotRegisteredBeerNameIsGivenThenThrowException() {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.empty());

        assertThrows(BeerNotFoundException.class, () -> beerService.findByName(expectedBeerDTO.getName()));
    }

    @Test
    void whenListBeerIsCalledThenReturnList() {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = createBeerFromDTO(expectedBeerDTO);

        when(beerRepository.findAll()).thenReturn(Collections.singletonList(expectedBeer));
        when(beerMapper.toDTO(expectedBeer)).thenReturn(expectedBeerDTO);

        List<BeerDTO> foundList = beerService.listAll();

        assertThat(foundList, is(not(empty())));
        assertThat(foundList.get(0), is(equalTo(expectedBeerDTO)));
    }

    @Test
    void whenListBeerIsEmptyThenReturnEmptyList() {
        when(beerRepository.findAll()).thenReturn(Collections.emptyList());

        List<BeerDTO> foundList = beerService.listAll();

        assertThat(foundList, is(empty()));
    }

    @Test
    void whenExclusionCalledWithValidIdThenBeerDeleted() throws BeerNotFoundException {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = createBeerFromDTO(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        doNothing().when(beerRepository).deleteById(expectedBeerDTO.getId());

        beerService.deleteById(expectedBeerDTO.getId());

        verify(beerRepository, times(1)).findById(expectedBeerDTO.getId());
        verify(beerRepository, times(1)).deleteById(expectedBeerDTO.getId());
    }

    @Test
    void whenIncrementCalledThenIncreaseStock() throws BeerNotFoundException, BeerStockExceededException {
        BeerDTO initialBeerDTO = BeerDTOBuilder.builder().quantity(10).max(50).build().toBeerDTO();
        // Usamos createBeerFromDTO para garantir o nome
        Beer initialBeer = createBeerFromDTO(initialBeerDTO);

        when(beerRepository.findById(initialBeerDTO.getId())).thenReturn(Optional.of(initialBeer));
        when(beerRepository.save(any(Beer.class))).thenAnswer(i -> i.getArguments()[0]);

        int quantityToIncrement = 10;
        int expectedQuantity = initialBeer.getQuantity() + quantityToIncrement;

        BeerDTO incrementedDTO = BeerDTOBuilder.builder().quantity(expectedQuantity).max(50).name(initialBeerDTO.getName()).build().toBeerDTO();
        when(beerMapper.toDTO(any(Beer.class))).thenReturn(incrementedDTO);

        BeerDTO incremented = beerService.increment(initialBeerDTO.getId(), quantityToIncrement);

        assertThat(incremented.getQuantity(), is(equalTo(expectedQuantity)));
        assertThat(incremented.getQuantity(), lessThan(initialBeerDTO.getMax()));
    }

    @Test
    void whenIncrementExceedsMaxThenThrowException() {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().quantity(45).max(50).build().toBeerDTO();
        Beer expectedBeer = createBeerFromDTO(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), 80));
    }

    @Test
    void whenIncrementWithInvalidIdThenThrowException() {
        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

        assertThrows(BeerNotFoundException.class, () -> beerService.increment(INVALID_BEER_ID, 10));
    }

    @Test
    void whenDecrementCalledThenDecreaseStock() throws BeerNotFoundException, BeerStockExceededException {
        BeerDTO initialBeerDTO = BeerDTOBuilder.builder().quantity(10).max(50).build().toBeerDTO();
        Beer initialBeer = createBeerFromDTO(initialBeerDTO);

        when(beerRepository.findById(initialBeerDTO.getId())).thenReturn(Optional.of(initialBeer));
        when(beerRepository.save(any(Beer.class))).thenAnswer(i -> i.getArguments()[0]);

        int quantityToDecrement = 5;
        int expectedQuantity = initialBeer.getQuantity() - quantityToDecrement;

        BeerDTO decrementedDTO = BeerDTOBuilder.builder().quantity(expectedQuantity).max(50).name(initialBeerDTO.getName()).build().toBeerDTO();
        when(beerMapper.toDTO(any(Beer.class))).thenReturn(decrementedDTO);

        BeerDTO decremented = beerService.decrement(initialBeerDTO.getId(), quantityToDecrement);

        assertThat(decremented.getQuantity(), is(equalTo(expectedQuantity)));
        assertThat(decremented.getQuantity(), greaterThanOrEqualTo(0));
    }

    @Test
    void whenDecrementToEmptyStockThenQuantityIsZero() throws BeerNotFoundException, BeerStockExceededException {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().quantity(10).max(50).build().toBeerDTO();
        Beer expectedBeer = createBeerFromDTO(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        when(beerRepository.save(any(Beer.class))).thenAnswer(i -> i.getArguments()[0]);

        int quantityToDecrement = expectedBeerDTO.getQuantity();
        int expectedQuantity = 0;

        BeerDTO decrementedDTO = BeerDTOBuilder.builder().quantity(expectedQuantity).max(50).name(expectedBeerDTO.getName()).build().toBeerDTO();
        when(beerMapper.toDTO(any(Beer.class))).thenReturn(decrementedDTO);

        BeerDTO decremented = beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement);

        assertThat(decremented.getQuantity(), is(expectedQuantity));
    }

    @Test
    void whenDecrementLowerThanZeroThenThrowException() {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().quantity(5).max(50).build().toBeerDTO();
        Beer expectedBeer = createBeerFromDTO(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        assertThrows(BeerStockExceededException.class, () -> beerService.decrement(expectedBeerDTO.getId(), 100));
    }

    @Test
    void whenDecrementWithInvalidIdThenThrowException() {
        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

        assertThrows(BeerNotFoundException.class, () -> beerService.decrement(INVALID_BEER_ID, 5));
    }
}