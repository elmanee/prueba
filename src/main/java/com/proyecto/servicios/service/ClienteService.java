package com.proyecto.servicios.service;

import com.proyecto.servicios.dto.ClienteRegistroDTO;
import com.proyecto.servicios.dto.ClienteResponseDTO;
import com.proyecto.servicios.dto.ClienteSearchRequestDTO;
import com.proyecto.servicios.dto.ClienteUpdateDTO;
import com.proyecto.servicios.entity.Cliente;
import com.proyecto.servicios.entity.Cuenta;
import com.proyecto.servicios.entity.Domicilio;
import com.proyecto.servicios.entity.Usuario;
import com.proyecto.servicios.exception.*;
import com.proyecto.servicios.repository.ClienteRepository;
import com.proyecto.servicios.repository.ClienteSpecification;
import com.proyecto.servicios.repository.CuentaRepository;
import com.proyecto.servicios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de negocio para la administración de clientes.
 * Devuelve siempre DTOs de respuesta, nunca entidades JPA directamente.
 */
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;
    private final PasswordEncoder passwordEncoder;

    // -------------------------------------------------------------------------
    // Registro
    // -------------------------------------------------------------------------

    @Transactional
    public ClienteResponseDTO registrarCliente(ClienteRegistroDTO dto) {
        if (Period.between(dto.getFechaNacimiento(), LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("El cliente debe ser mayor de 18 años.");
        }

        if (clienteRepository.existsByCurp(dto.getCurp())) {
            throw new CurpDuplicadaException("El CURP ya existe: " + dto.getCurp());
        }
        if (clienteRepository.existsByRfc(dto.getRfc())) {
            throw new RfcDuplicadaException("El RFC ya existe: " + dto.getRfc());
        }
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new ClienteYaRegistradoException("El correo ya está registrado: " + dto.getCorreo());
        }

        Cliente nuevoCliente = Cliente.builder()
                .nombre(dto.getNombre())
                .segundoNombre(dto.getSegundoNombre())
                .apellidoPaterno(dto.getApellidoPaterno())
                .apellidoMaterno(dto.getApellidoMaterno())
                .curp(dto.getCurp())
                .rfc(dto.getRfc())
                .fechaNacimiento(dto.getFechaNacimiento())
                .sexo(dto.getSexo())
                .nacionalidad(dto.getNacionalidad())
                .estadoCivil(dto.getEstadoCivil())
                .telefonoMovil(dto.getTelefonoMovil())
                .telefonoAlternativo(dto.getTelefonoAlternativo())
                .ocupacion(dto.getOcupacion())
                .empresa(dto.getEmpresa())
                .ingresoMensual(dto.getIngresoMensual())
                .activo(true)
                .build();

        nuevoCliente = clienteRepository.save(nuevoCliente);

        Usuario nuevoUsuario = Usuario.builder()
                .cliente(nuevoCliente)
                .correo(dto.getCorreo())
                .passwordHash(passwordEncoder.encode(dto.getRawPassword()))
                .estatus("ACTIVO")
                .build();
        usuarioRepository.save(nuevoUsuario);

        Cuenta nuevaCuenta = Cuenta.builder()
                .cliente(nuevoCliente)
                .numeroCuenta(String.valueOf((long) (Math.random() * 100_000_000_000_000L)))
                .moneda("MXN")
                .saldo(new BigDecimal("10.00"))
                .tipoCuenta("DEBITO")
                .build();
        cuentaRepository.save(nuevaCuenta);

        return toDTO(nuevoCliente, dto.getCorreo());
    }

    // -------------------------------------------------------------------------
    // Búsqueda dinámica — UN ÚNICO endpoint para múltiples criterios
    // -------------------------------------------------------------------------

    public List<ClienteResponseDTO> buscar(ClienteSearchRequestDTO req) {
        return clienteRepository.findAll(ClienteSpecification.fromSearchRequest(req))
                .stream()
                .map(c -> toDTO(c, c.getUsuario() != null ? c.getUsuario().getCorreo() : null))
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Baja lógica
    // -------------------------------------------------------------------------

    @Transactional
    public void darDeBaja(ClienteSearchRequestDTO req) {
        if (req.getId() == null) {
            throw new IllegalArgumentException("Se debe proporcionar el id del cliente para darlo de baja.");
        }
        Cliente cliente = clienteRepository.findById(req.getId())
                .orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado con id: " + req.getId()));
        cliente.setActivo(false);
        if (cliente.getUsuario() != null) {
            cliente.getUsuario().setEstatus("INACTIVO");
        }
        clienteRepository.save(cliente);
    }

    // -------------------------------------------------------------------------
    // Actualización
    // -------------------------------------------------------------------------

    @Transactional
    public ClienteResponseDTO actualizarCliente(ClienteSearchRequestDTO busqueda, ClienteUpdateDTO dto) {
        if (busqueda.getId() == null) {
            throw new IllegalArgumentException("Se debe proporcionar el id del cliente a actualizar.");
        }
        Cliente cliente = clienteRepository.findById(busqueda.getId())
                .orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado con id: " + busqueda.getId()));

        if (dto.getNombre() != null) cliente.setNombre(dto.getNombre());
        if (dto.getSegundoNombre() != null) cliente.setSegundoNombre(dto.getSegundoNombre());
        if (dto.getApellidoPaterno() != null) cliente.setApellidoPaterno(dto.getApellidoPaterno());
        if (dto.getApellidoMaterno() != null) cliente.setApellidoMaterno(dto.getApellidoMaterno());
        if (dto.getSexo() != null) cliente.setSexo(dto.getSexo());
        if (dto.getNacionalidad() != null) cliente.setNacionalidad(dto.getNacionalidad());
        if (dto.getEstadoCivil() != null) cliente.setEstadoCivil(dto.getEstadoCivil());
        if (dto.getTelefonoMovil() != null) cliente.setTelefonoMovil(dto.getTelefonoMovil());
        if (dto.getTelefonoAlternativo() != null) cliente.setTelefonoAlternativo(dto.getTelefonoAlternativo());
        if (dto.getOcupacion() != null) cliente.setOcupacion(dto.getOcupacion());
        if (dto.getEmpresa() != null) cliente.setEmpresa(dto.getEmpresa());
        if (dto.getIngresoMensual() != null) cliente.setIngresoMensual(dto.getIngresoMensual());

        if (dto.getFechaNacimiento() != null) {
            if (Period.between(dto.getFechaNacimiento(), LocalDate.now()).getYears() < 18) {
                throw new IllegalArgumentException("El cliente debe ser mayor de 18 años.");
            }
            cliente.setFechaNacimiento(dto.getFechaNacimiento());
        }

        if (dto.getCorreo() != null && cliente.getUsuario() != null
                && !cliente.getUsuario().getCorreo().equals(dto.getCorreo())) {
            if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
                throw new ClienteYaRegistradoException("El correo ya está registrado: " + dto.getCorreo());
            }
            cliente.getUsuario().setCorreo(dto.getCorreo());
        }

        if (dto.getCalle() != null || dto.getNumeroExterior() != null || dto.getColonia() != null) {
            Domicilio domicilio = new Domicilio();
            domicilio.setCliente(cliente);
            domicilio.setCalle(dto.getCalle());
            domicilio.setNumeroExterior(dto.getNumeroExterior());
            domicilio.setNumeroInterior(dto.getNumeroInterior());
            domicilio.setColonia(dto.getColonia());
            domicilio.setCodigoPostal(dto.getCodigoPostal());
            domicilio.setMunicipio(dto.getMunicipio());
            domicilio.setEstado(dto.getEstado());
            domicilio.setPais(dto.getPais());

            if (cliente.getDomicilios() == null) {
                cliente.setDomicilios(List.of(domicilio));
            } else {
                cliente.getDomicilios().add(domicilio);
            }
        }

        Cliente actualizado = clienteRepository.save(cliente);
        String correo = actualizado.getUsuario() != null ? actualizado.getUsuario().getCorreo() : null;
        return toDTO(actualizado, correo);
    }

    // -------------------------------------------------------------------------
    // Mapeo entidad → DTO
    // -------------------------------------------------------------------------

    private ClienteResponseDTO toDTO(Cliente c, String correo) {
        return ClienteResponseDTO.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .segundoNombre(c.getSegundoNombre())
                .apellidoPaterno(c.getApellidoPaterno())
                .apellidoMaterno(c.getApellidoMaterno())
                .curp(c.getCurp())
                .rfc(c.getRfc())
                .fechaNacimiento(c.getFechaNacimiento())
                .sexo(c.getSexo())
                .nacionalidad(c.getNacionalidad())
                .estadoCivil(c.getEstadoCivil())
                .telefonoMovil(c.getTelefonoMovil())
                .telefonoAlternativo(c.getTelefonoAlternativo())
                .ocupacion(c.getOcupacion())
                .empresa(c.getEmpresa())
                .ingresoMensual(c.getIngresoMensual())
                .activo(c.getActivo())
                .creadoEn(c.getCreadoEn())
                .correo(correo)
                .build();
    }
}