-- phpMyAdmin SQL Dump
-- version 5.2.3deb1
-- https://www.phpmyadmin.net/
--
-- Servidor: localhost:3306
-- Tiempo de generación: 19-06-2026 a las 19:07:24
-- Versión del servidor: 11.8.6-MariaDB-5 from Ubuntu
-- Versión de PHP: 8.5.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `domitila_db`
--
CREATE DATABASE IF NOT EXISTS `domitila_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_spanish_nopad_ai_ci;
USE `domitila_db`;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `personal_laboral`
--

CREATE TABLE `personal_laboral` (
  `id_trabajador` int(11) NOT NULL,
  `nombre` varchar(32) NOT NULL,
  `apellido1` varchar(24) NOT NULL,
  `apellido2` varchar(24) DEFAULT NULL,
  `clave` varchar(256) NOT NULL,
  `telefono` varchar(255) NOT NULL,
  `convenio_laboral` varchar(32) DEFAULT NULL,
  `correo_electronico` varchar(64) NOT NULL,
  `dias_vacaciones` int(11) DEFAULT NULL,
  `discapacidad` bit(1) NOT NULL,
  `dni` varchar(15) NOT NULL,
  `domicilio_completo` varchar(255) DEFAULT NULL,
  `fecha_alta` date DEFAULT NULL,
  `fecha_baja` date DEFAULT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `grupo_profesional` varchar(8) NOT NULL,
  `horas_jornada_parcial` decimal(10,2) DEFAULT NULL,
  `id_categoria_profesional` int(11) NOT NULL,
  `numero_cuenta` varchar(34) DEFAULT NULL,
  `numero_hijos` int(11) NOT NULL,
  `salario_bruto` decimal(10,2) DEFAULT NULL,
  `sexo` varchar(16) NOT NULL,
  `tipo_contrato` varchar(16) NOT NULL,
  `tipo_jornada` varchar(16) NOT NULL,
  `titulacion` varchar(150) DEFAULT NULL,
  `vehiculo` bit(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_spanish_nopad_ai_ci;

--
-- Volcado de datos para la tabla `personal_laboral`
--

INSERT INTO `personal_laboral` (`id_trabajador`, `nombre`, `apellido1`, `apellido2`, `clave`, `telefono`, `convenio_laboral`, `correo_electronico`, `dias_vacaciones`, `discapacidad`, `dni`, `domicilio_completo`, `fecha_alta`, `fecha_baja`, `fecha_nacimiento`, `grupo_profesional`, `horas_jornada_parcial`, `id_categoria_profesional`, `numero_cuenta`, `numero_hijos`, `salario_bruto`, `sexo`, `tipo_contrato`, `tipo_jornada`, `titulacion`, `vehiculo`) VALUES
(1, 'Laura', 'Hernández', 'González', '$2a$10$sWnSC6Wcsu.JDmW.62nG6eh66.2Lg24vLDNW8J4ngm9OpFe1ANE.W', '600000000', NULL, 'laura.hernandez@gmail.com', NULL, b'0', '12345678Z', NULL, NULL, NULL, NULL, '', NULL, 0, NULL, 0, NULL, '', '', '', NULL, b'0'),
(2, 'María', 'González', 'Rodríguez', '$2a$10$jRhjGN.J3R2tzB2/7j6MjeiYYrUhG4qWm0moiOM7dxr.l1AneafZW', '611111111', NULL, 'maria.gonzalez@gmail.com', NULL, b'0', '87654321X', NULL, NULL, NULL, NULL, '', NULL, 0, NULL, 0, NULL, '', '', '', NULL, b'0'),
(3, 'Carlos', 'Chiqui', 'Pereyra', '$2a$10$nQGkSxM/z/fwEHUEOdZfa.piCmUdwcOHwb8UsCNkfvwdd0lxXiUAi', '622222222', NULL, 'carlos.chiqui@gmail.com', NULL, b'0', '45678901Y', NULL, NULL, NULL, NULL, '', NULL, 0, NULL, 0, NULL, '', '', '', NULL, b'0');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `personal_laboral_role`
--

CREATE TABLE `personal_laboral_role` (
  `trabajador_id` int(11) NOT NULL,
  `role_id` enum('ADMIN','DELEGADO','TECNICO') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_spanish_nopad_ai_ci;

--
-- Volcado de datos para la tabla `personal_laboral_role`
--

INSERT INTO `personal_laboral_role` (`trabajador_id`, `role_id`) VALUES
(1, 'ADMIN'),
(3, 'TECNICO');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `personal_laboral`
--
ALTER TABLE `personal_laboral`
  ADD PRIMARY KEY (`id_trabajador`),
  ADD UNIQUE KEY `telefono` (`telefono`),
  ADD UNIQUE KEY `correo_electronico` (`correo_electronico`),
  ADD UNIQUE KEY `dni` (`dni`);

--
-- Indices de la tabla `personal_laboral_role`
--
ALTER TABLE `personal_laboral_role`
  ADD PRIMARY KEY (`trabajador_id`,`role_id`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `personal_laboral`
--
ALTER TABLE `personal_laboral`
  MODIFY `id_trabajador` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `personal_laboral_role`
--
ALTER TABLE `personal_laboral_role`
  ADD CONSTRAINT `personal_laboral_role_ibfk_1` FOREIGN KEY (`trabajador_id`) REFERENCES `personal_laboral` (`id_trabajador`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
