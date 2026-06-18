-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Jun 18, 2026 at 10:50 AM
-- Server version: 12.2.2-MariaDB
-- PHP Version: 8.5.6

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `tecnicos`
--
CREATE DATABASE IF NOT EXISTS `tecnicos` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_spanish_nopad_ai_ci;
USE `tecnicos`;

-- --------------------------------------------------------

--
-- Table structure for table `role`
--

CREATE TABLE `role` (
  `id` bigint(20) NOT NULL,
  `name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_nopad_ci;

--
-- Dumping data for table `role`
--

INSERT INTO `role` (`id`, `name`) VALUES
(1, 'ADMIN'),
(2, 'DELEGADO'),
(3, 'TECNICO');

-- --------------------------------------------------------

--
-- Table structure for table `tecnico`
--

CREATE TABLE `tecnico` (
  `id` bigint(20) NOT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `apellido1` varchar(255) DEFAULT NULL,
  `apellido2` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `clave` varchar(256) NOT NULL,
  `telefono` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_spanish_nopad_ai_ci;

--
-- Dumping data for table `tecnico`
--

INSERT INTO `tecnico` (`id`, `nombre`, `apellido1`, `apellido2`, `email`, `clave`, `telefono`) VALUES
(1, 'Laura', 'Hernández', 'Domitila', 'laura.hernandez@gmail.com', '$2a$12$XVPo2FFGNpyk3pgudQlwvuK30uflP0jvfSfNY3YBYCEkXQ7IauHgW', '611111111', 1),
(2, 'Juan', 'Pérez', '', 'juan.perez@example.com', '$2a$10$9U1pnd33qHcKX96s6EK3..rwX4Etkt8eutCaY6FuB5/O8avSqK7vm', '600123456'),
(3, 'Juan', 'Gómez', '', 'juan.gomez@example.com', '$2a$10$AMHOqg0rt6vALstDTEomA.YeRfXad.3WeNephTe1rRNBDZS.oYHoW', '611123456');

-- --------------------------------------------------------

--
-- Table structure for table `tecnico_role`
--

CREATE TABLE `tecnico_role` (
  `tecnico_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_spanish_nopad_ai_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `role`
--
ALTER TABLE `role`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `tecnico`
--
ALTER TABLE `tecnico`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`,`telefono`);

--
-- Indexes for table `tecnico_role`
--
ALTER TABLE `tecnico_role`
  ADD KEY `tecnico_id` (`tecnico_id`),
  ADD KEY `role_id` (`role_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `tecnico`
--
ALTER TABLE `tecnico`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `tecnico_role`
--
ALTER TABLE `tecnico_role`
  ADD CONSTRAINT `1` FOREIGN KEY (`tecnico_id`) REFERENCES `tecnico` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `2` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
