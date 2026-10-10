package com.proyecto.servicios.service;

import com.proyecto.servicios.dto.ClienteUpdateDTO;
import com.proyecto.servicios.dto.ClienteRegistroDTO;
import com.proyecto.servicios.entity.Cliente;
import com.proyecto.servicios.entity.Cuenta;
import com.proyecto.servicios.entity.Usuario;
import com.proyecto.servicios.repository.ClienteRepository;
import com.proyecto.servicios.repository.CuentaRepository;
import com.proyecto.servicios.repository.UsuarioRepository;
import com.proyecto.servicios.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Cliente registrarCliente(ClienteRegistroDTO dto) {
        if (Period.between(dto.getFechaNacimiento(), LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("El cliente debe ser mayor de 18 años.");
        }

        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (dto.getRawPassword() == null || !dto.getRawPassword().matches(passwordRegex)) {
            throw new IllegalArgumentException("La contraseña debe tener mínimo 8 caracteres, al menos una mayúscula, una minúscula, un número y un carácter especial.");
        }

        if (clienteRepository.existsByCurp(dto.getCurp())) throw new CurpDuplicadaException("El CURP ya existe.");
        if (clienteRepository.existsByRfc(dto.getRfc())) throw new RfcDuplicadaException("El RFC ya existe.");
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) throw new ClienteYaRegistradoException("El correo ya está registrado.");

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
                .numeroCuenta(String.valueOf((long)(Math.random() * 100000000000000L)))
                .moneda("MXN")
                .saldo(new BigDecimal("10.00"))
                .tipoCuenta("DEBITO")
                .build();
        cuentaRepository.save(nuevaCuenta);

        return nuevoCliente;
    }

    public List<Cliente> obtenerClientesActivos() {
        return clienteRepository.findByActivoTrue();
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado"));
    }

    public Cliente buscarPorCurp(String curp) {
        return clienteRepository.findByCurp(curp).orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado por CURP"));
    }

    public Cliente buscarPorRfc(String rfc) {
        return clienteRepository.findByRfc(rfc).orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado por RFC"));
    }

    @Transactional
    public void darDeBaja(Long id) {
        Cliente cliente = buscarPorId(id);
        cliente.setActivo(false);
        if (cliente.getUsuario() != null) {
            cliente.getUsuario().setEstatus("INACTIVO");
        }
        clienteRepository.save(cliente);
    }

    public Cliente buscarPorCorreo(String correo) {
        return clienteRepository.findByUsuarioCorreo(correo)
                .orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado por correo"));
    }

    public List<Cuenta> obtenerCuentasActivas() {
        return cuentaRepository.findByActivoTrue();
    }

    public BigDecimal obtenerSaldoCuenta(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException("Cuenta no encontrada"));
        return cuenta.getSaldo();
    }

    public List<Cliente> buscarPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return clienteRepository.findByCreadoEnBetween(inicio, fin);
    }

    @Transactional
    public Cliente actualizarCliente(Long id, ClienteUpdateDTO dto) {
        Cliente cliente = buscarPorId(id);
        
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

        if (dto.getCorreo() != null && cliente.getUsuario() != null && !cliente.getUsuario().getCorreo().equals(dto.getCorreo())) {
            if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
                throw new ClienteYaRegistradoException("El correo ya está registrado.");
            }
            cliente.getUsuario().setCorreo(dto.getCorreo());
        }

        if (dto.getCalle() != null || dto.getNumeroExterior() != null || dto.getColonia() != null) {
            com.proyecto.servicios.entity.Domicilio domicilio = new com.proyecto.servicios.entity.Domicilio();
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
                cliente.setDomicilios(java.util.List.of(domicilio));
            } else {
                cliente.getDomicilios().add(domicilio);
            }
        }

        return clienteRepository.save(cliente);
    }
}