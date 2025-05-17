-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 16-05-2025 a las 15:53:31
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `localfresh`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `admin_notifications`
--

CREATE TABLE `admin_notifications` (
  `notification_id` int(11) NOT NULL,
  `notification_type` varchar(50) NOT NULL,
  `related_id` int(11) NOT NULL,
  `message` text NOT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `admin_notifications`
--

INSERT INTO `admin_notifications` (`notification_id`, `notification_type`, `related_id`, `message`, `is_read`, `created_at`) VALUES
(1, 'review_report', 1, 'La reseña #1 ha sido reportada 3 veces. Motivo más reciente: inappropriate', 1, '2025-04-17 00:58:11'),
(2, 'review_report', 53, 'La reseña #53 ha sido reportada 3 veces. Motivo más reciente: inappropriate', 1, '2025-04-17 01:10:57'),
(3, 'review_report', 58, 'La reseña #58 ha sido reportada 3 veces. Motivo más reciente: inappropriate', 1, '2025-04-17 01:33:55'),
(4, 'review_report', 1, 'La reseña #1 ha sido reportada 3 veces. Motivo más reciente: inappropriate', 1, '2025-04-17 18:24:10'),
(5, 'review_report', 62, 'La reseña #62 ha sido reportada 3 veces. Motivo más reciente: inappropriate', 1, '2025-04-22 18:29:57'),
(6, 'review_report', 1, 'La reseña #1 ha sido reportada 3 veces. Motivo más reciente: spam', 1, '2025-04-22 18:38:03'),
(7, 'review_report', 1, 'La reseña #1 ha sido reportada 3 veces. Motivo más reciente: fraudulent', 1, '2025-04-22 18:42:06'),
(8, 'review_report', 1, 'La reseña #1 ha sido reportada 3 veces. Motivo más reciente: fraudulent', 1, '2025-04-22 18:43:26'),
(9, 'review_report', 62, 'La reseña #62 ha sido reportada 3 veces. Motivo más reciente: spam', 0, '2025-04-23 06:38:34'),
(10, 'review_report', 53, 'La reseña #53 ha sido reportada 3 veces. Motivo más reciente: spam', 0, '2025-04-27 00:37:46'),
(11, 'review_report', 66, 'La reseña #66 ha sido reportada 3 veces. Motivo más reciente: inappropriate', 0, '2025-05-02 16:46:26'),
(12, 'review_report', 66, 'La reseña #66 ha sido reportada 3 veces. Motivo más reciente: inappropriate', 0, '2025-05-02 16:53:49'),
(13, 'review_report', 66, 'La reseña #66 ha sido reportada 3 veces. Motivo más reciente: inappropriate', 0, '2025-05-02 16:55:43'),
(14, 'review_report', 66, 'La reseña #66 ha sido reportada 3 veces. Motivo más reciente: inappropriate', 0, '2025-05-02 17:05:32'),
(15, 'review_report', 66, 'La reseña #66 ha sido reportada 3 veces. Motivo más reciente: other', 0, '2025-05-02 17:12:44');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `carts`
--

CREATE TABLE `carts` (
  `cart_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `seller_id` int(11) NOT NULL,
  `status` enum('activo','reservado','completado','expirado') DEFAULT 'activo',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `expiration_time` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `carts`
--

INSERT INTO `carts` (`cart_id`, `user_id`, `seller_id`, `status`, `created_at`, `expiration_time`) VALUES
(184, 40, 14, 'reservado', '2025-04-15 23:58:04', '2025-04-15 18:28:04'),
(187, 41, 14, 'reservado', '2025-04-18 21:51:58', '2025-04-18 16:21:58'),
(216, 42, 14, 'reservado', '2025-04-27 03:56:13', '2025-04-26 22:26:13'),
(243, 38, 14, 'reservado', '2025-05-04 20:31:44', '2025-05-04 23:01:44'),
(244, 38, 14, 'reservado', '2025-05-05 02:39:21', '2025-05-04 21:09:48');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cart_items`
--

CREATE TABLE `cart_items` (
  `item_id` int(11) NOT NULL,
  `cart_id` int(11) NOT NULL,
  `unit_id` int(11) NOT NULL,
  `added_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `quantity` int(11) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `cart_items`
--

INSERT INTO `cart_items` (`item_id`, `cart_id`, `unit_id`, `added_at`, `quantity`) VALUES
(286, 184, 101, '2025-04-15 23:58:04', 2),
(320, 216, 102, '2025-04-27 03:56:13', 2),
(355, 243, 122, '2025-05-04 20:31:44', 1),
(356, 244, 122, '2025-05-05 02:39:21', 3);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `favorite_products`
--

CREATE TABLE `favorite_products` (
  `favorite_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `added_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `favorite_products`
--

INSERT INTO `favorite_products` (`favorite_id`, `user_id`, `product_id`, `added_at`) VALUES
(42, 38, 61, '2025-04-18 03:53:31'),
(43, 38, 55, '2025-04-18 03:53:34'),
(44, 38, 59, '2025-04-19 05:02:31'),
(45, 38, 57, '2025-04-22 18:39:40'),
(46, 42, 61, '2025-04-24 05:46:26'),
(48, 38, 69, '2025-04-30 19:06:28');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `favorite_stores`
--

CREATE TABLE `favorite_stores` (
  `favorite_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `seller_id` int(11) NOT NULL,
  `added_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `favorite_stores`
--

INSERT INTO `favorite_stores` (`favorite_id`, `user_id`, `seller_id`, `added_at`) VALUES
(39, 40, 14, '2025-04-16 01:43:28'),
(46, 38, 14, '2025-04-18 03:42:22'),
(49, 41, 14, '2025-04-18 21:51:53'),
(51, 42, 14, '2025-04-20 03:12:04');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `password_resets`
--

CREATE TABLE `password_resets` (
  `id` int(11) NOT NULL,
  `email` varchar(255) NOT NULL,
  `token` varchar(255) NOT NULL,
  `expires` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `password_resets`
--

INSERT INTO `password_resets` (`id`, `email`, `token`, `expires`) VALUES
(48, 'naimbadpiggie@gmail.com', '72f5f0791978b4e93bdf47c2c3eb2f758970269da00de6e6c5cc71ea892b41067e412cc98cda20574fb1de10a92f379b9098', '2025-05-02 12:15:22'),
(49, 'einarnaim839@gmail.com', '10932218b0e610da754ae076b253a6eb254e8ea19959a09d372360e00f1bbf17a761607972437439744ba9d0ba8e1d764d88', '2025-05-02 12:17:58'),
(50, 'einarnaim839@gmail.com', '3f336823cfe791293bfcc8a9457e44601434d5e2f15a8d2129dfae836727c26c8352a0c20f9138a2fa5edea3da6dca091dac', '2025-05-02 12:19:46'),
(51, 'einarnaim839@gmail.com', 'c836e33b234eebc2fc07d133c3483c40151e61ebe5f6d93abef5c1c910eded689e1f406cb977676c4dc05751b8ca63b815c5', '2025-05-02 12:25:58'),
(52, 'einarnaim839@gmail.com', '8e1181ea9ed0456be7348fc987a3957183bb6ccd829f1adc9f8f267932c1bfb4be0022f0e60e01351a3d7c8ce74e5aa4772b', '2025-05-02 12:25:58'),
(53, 'einarnaim839@gmail.com', 'e4806d72d6b451dac6d1889a6662c774e61e967b5d95a2c63338bbb2e68e434e551eb525db800b503872930791bbd432bc1d', '2025-05-02 12:25:58'),
(54, 'einarnaim839@gmail.com', '40a7632c793c3c98bc55f34fa4ad13faf9e8d101688b887df40ea0082c61d80043197adc2a2e23b84064ec27320268b70fd1', '2025-05-02 12:25:58'),
(55, 'einarnaim839@gmail.com', 'f28e86ad222a81c1b0b57fbacdfcc403afbacedfb9d0b5147ab8e38f3008dea7ab6165aec8f708cda7d0119b16bccf2a23b9', '2025-05-02 12:25:59');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `products`
--

CREATE TABLE `products` (
  `product_id` int(11) NOT NULL,
  `seller_id` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `expiry_type` enum('caducidad','consumo_preferente') NOT NULL,
  `image_url` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `products`
--

INSERT INTO `products` (`product_id`, `seller_id`, `name`, `description`, `category`, `price`, `expiry_type`, `image_url`) VALUES
(47, 16, 'Plato Gourmet', 'Un plato gourmet', 'Legumbres', 150, 'caducidad', 'http://10.0.2.2/localfresh/uploads/Abarrotes_De_La_Esquina/products/1741224356_Plato_Gourmet.png'),
(52, 14, 'Leche entera lala', 'Leche pasteurizada 1L', 'Lácteos', 30, 'caducidad', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Migue/products/52_1741475483.png'),
(53, 14, 'Queso manchego', 'Queso Navarro 1KG', 'Lácteos', 70, 'caducidad', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Migue/products/53_1741475504.png'),
(54, 14, 'Huevo rojo', 'Docena de huevos frescos', 'Huevos', 50, 'consumo_preferente', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Migue/products/54_1741475514.png'),
(55, 14, 'Arroz integral', 'Arroz de grano largo 1kg', 'Granos y cereales', 35, 'consumo_preferente', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Migue/products/55_1741475523.png'),
(56, 14, 'Frijol Negro Valle', 'Frijol en grano 1kg', 'Legumbres', 40, 'consumo_preferente', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Migue/products/56_1741475546.png'),
(57, 14, 'Pan Bimbo', 'Pan bimbo blanco', 'Panadería y repostería', 38, 'caducidad', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Migue/products/57_1741475553.png'),
(58, 14, 'Jumex de naranja', 'Jumex 1L sabor naranja', 'Bebidas', 30, 'caducidad', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Migue/products/58_1741475562.png'),
(59, 14, 'Sabritas Adobadas', 'Bolsa de sabritas 170g de pura delicia', 'Snacks y botanas', 20, 'consumo_preferente', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Migue/products/59_1741475570.png'),
(61, 14, 'Sobre pedigree ', 'Alimento Adulto Res', 'Comida para mascotas', 30, 'consumo_preferente', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Migue/products/61_1741475579.png'),
(63, 14, 'Yoghurt natural Danone', 'Yoghurt sin azúcar 900g', 'Lácteos', 55, 'caducidad', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Migue/products/63_1741475711.png'),
(64, 14, 'Huevos San Juan', 'Docena de huevos blancos frescos', 'Huevos', 52, 'consumo_preferente', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Migue/products/64_1741475717.png'),
(65, 14, 'Avena Quaker', 'Hojuelas de avena natural 800g', 'Granos y cereales', 48, 'consumo_preferente', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Migue/products/65_1741475724.png'),
(66, 14, 'Lentejas Verde Valle', 'Bolsa de lentejas 1kg', 'Legumbres', 42, 'consumo_preferente', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Migue/products/66_1741475733.png'),
(67, 14, 'Donas Bimbo', 'Pan Dulce Donas Bimbo 158 grs', 'Panadería y repostería', 25, 'caducidad', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Migue/products/67_1741475740.png'),
(68, 14, 'Refresco Coca-Cola', 'Botella de refresco de cola 2L', 'Bebidas', 40, 'consumo_preferente', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Migue/products/68_1741475747.png'),
(69, 14, 'Doritos Nachos', 'Bolsa de totopos sabor queso 76g', 'Snacks y botanas', 35, 'consumo_preferente', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Migue/products/69_1741475759.png'),
(70, 14, 'Helado Holanda', 'Helado Fresa Cremissimo Holanda 900 ml', 'Congelados', 90, 'caducidad', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Migue/products/70_1741475766.png'),
(71, 14, 'Croquetas Dog Chow', 'Alimento para perros adultos 4kg', 'Comida para mascotas', 320, 'consumo_preferente', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Migue/products/71_1741475775.png'),
(74, 17, 'Pollo A La Naranja', 'Pollo a la naranja de las bolas de arroz', 'Snacks y botanas', 60, 'caducidad', 'http://10.0.2.2/localfresh/uploads/Tienda_3/products/1743124576_Pollo_A_La_Naranja.png'),
(77, 14, 'Refresco perfy', 'Refresco ponche de frutas', 'Bebidas', 60, 'consumo_preferente', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Juan/products/1745701702_Refresco_perfy.png'),
(79, 14, 'Prueba prodUcto optimizado', 'pruebita aca', 'Lácteos', 30, 'consumo_preferente', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Juan/products/1745805753_Prueba_prodUcto_optimizado.png');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `product_units`
--

CREATE TABLE `product_units` (
  `unit_id` int(11) NOT NULL,
  `product_id` int(11) DEFAULT NULL,
  `discount_price` double DEFAULT NULL,
  `expiry_date` date DEFAULT NULL,
  `status` enum('disponible','apartado','vendido','expirado') NOT NULL DEFAULT 'disponible',
  `quantity` int(11) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `product_units`
--

INSERT INTO `product_units` (`unit_id`, `product_id`, `discount_price`, `expiry_date`, `status`, `quantity`) VALUES
(46, 47, 150, '2025-03-10', 'expirado', 0),
(47, 47, 150, '2025-03-05', 'expirado', 0),
(48, 47, 150, '2025-05-24', 'disponible', 2),
(49, 67, 38.25, '2025-03-26', 'expirado', 1),
(50, 67, 45, '2025-03-15', 'expirado', 1),
(51, 67, 45, '2025-03-18', 'expirado', 1),
(59, 55, 35, '2025-04-30', 'disponible', 5),
(61, 56, 30, '2025-04-09', 'expirado', 0),
(63, 57, 38, '2025-03-21', 'expirado', 1),
(64, 57, 38, '2025-03-26', 'expirado', 1),
(73, 63, 55, '2025-03-27', 'expirado', 0),
(74, 63, 55, '2025-03-18', 'expirado', 1),
(76, 64, 52, '2025-03-22', 'expirado', 1),
(77, 65, 48, '2025-03-22', 'expirado', 0),
(78, 65, 48, '2025-03-22', 'expirado', 1),
(79, 66, 42, '2025-03-26', 'expirado', 1),
(96, 63, 27.5, '2025-03-28', 'expirado', 0),
(97, 74, 60, '2025-03-29', 'expirado', 0),
(98, 74, 60, '2025-03-29', 'expirado', 0),
(99, 74, 60, '2025-03-29', 'expirado', 0),
(100, 74, 60, '2025-03-29', 'expirado', 0),
(101, 57, 38, '2025-04-27', 'apartado', 0),
(102, 57, 38, '2025-04-28', 'expirado', 0),
(103, 57, 38, '2025-04-28', 'expirado', 0),
(105, 58, 30, '2025-04-28', 'expirado', 0),
(109, 58, 30, '2025-04-06', 'expirado', 0),
(110, 68, 28, '2025-04-12', 'expirado', 0),
(112, 70, 76.5, '2025-04-26', 'expirado', 0),
(114, 55, 29.75, '2025-04-30', 'disponible', 9),
(115, 61, 25.5, '2025-04-29', 'disponible', 10),
(121, 59, 20, '2025-05-24', 'disponible', 10),
(122, 59, 20, '2025-05-09', 'disponible', 9),
(123, 59, 17, '2025-04-30', 'disponible', 14),
(125, 59, 10, '2025-04-25', 'expirado', 0),
(126, 61, 30, '2025-06-28', 'disponible', 5),
(127, 77, 60, '2025-05-31', 'disponible', 7),
(131, 69, 24.5, '2025-04-30', 'disponible', 1),
(132, 56, 15, '2025-04-27', 'disponible', 5),
(133, 56, 20, '2025-04-30', 'disponible', 3),
(134, 52, 15, '2025-05-01', 'disponible', 5);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `reservations`
--

CREATE TABLE `reservations` (
  `reservation_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `seller_id` int(11) NOT NULL,
  `status` enum('Pendiente','Completado','Cancelado','Expirado') DEFAULT 'Pendiente',
  `reservation_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `expiration_date` datetime NOT NULL,
  `completed_date` datetime DEFAULT NULL,
  `total_price` decimal(10,2) NOT NULL,
  `original_price` decimal(10,2) DEFAULT NULL,
  `qr_code` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `reservations`
--

INSERT INTO `reservations` (`reservation_id`, `user_id`, `seller_id`, `status`, `reservation_date`, `expiration_date`, `completed_date`, `total_price`, `original_price`, `qr_code`) VALUES
(135, 38, 14, 'Completado', '2025-04-29 03:34:08', '2025-04-28 23:34:08', '2025-04-28 21:34:40', 192.00, 228.00, '91a96fc98e'),
(136, 38, 14, 'Cancelado', '2025-04-29 03:35:13', '2025-04-28 23:35:13', NULL, 112.50, NULL, '4c63022ed2'),
(137, 38, 14, 'Completado', '2025-04-30 01:43:22', '2025-04-29 21:43:22', '2025-04-29 19:44:32', 15.00, 30.00, '80368af57b'),
(138, 38, 14, 'Expirado', '2025-04-30 01:49:58', '2025-04-29 19:50:09', NULL, 45.00, 90.00, 'd839b31094'),
(139, 38, 14, 'Expirado', '2025-04-30 01:50:51', '2025-04-29 19:51:21', NULL, 30.00, 60.00, '633055ed9e'),
(140, 38, 14, 'Cancelado', '2025-04-30 19:19:28', '2025-04-30 15:19:28', NULL, 30.00, 60.00, 'd06403bb68'),
(141, 38, 14, 'Expirado', '2025-04-30 20:20:53', '2025-04-30 16:20:53', NULL, 65.00, 100.00, '5a9bd85146'),
(142, 38, 14, 'Completado', '2025-05-01 22:22:43', '2025-05-01 18:22:43', '2025-05-01 16:23:54', 20.00, 20.00, '3f8852fa0e'),
(143, 38, 14, 'Cancelado', '2025-05-05 04:31:52', '2025-05-05 00:31:52', NULL, 17.00, 20.00, 'd5034b9138'),
(144, 38, 14, 'Expirado', '2025-05-05 02:40:01', '2025-05-04 21:40:01', NULL, 51.00, 60.00, '7fcc574fed');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `reservation_units`
--

CREATE TABLE `reservation_units` (
  `reservation_unit_id` int(11) NOT NULL,
  `reservation_id` int(11) NOT NULL,
  `unit_id` int(11) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `reservation_units`
--

INSERT INTO `reservation_units` (`reservation_unit_id`, `reservation_id`, `unit_id`, `price`, `quantity`) VALUES
(177, 135, 122, 19.00, 2),
(178, 135, 127, 60.00, 2),
(179, 135, 102, 19.00, 1),
(180, 135, 115, 15.00, 1),
(181, 136, 114, 17.50, 3),
(182, 136, 126, 30.00, 2),
(183, 137, 115, 15.00, 1),
(184, 138, 115, 15.00, 3),
(185, 139, 123, 10.00, 3),
(186, 140, 123, 10.00, 1),
(187, 140, 133, 20.00, 1),
(188, 141, 59, 17.50, 2),
(189, 141, 126, 30.00, 1),
(190, 142, 121, 20.00, 1),
(191, 143, 122, 17.00, 1),
(192, 144, 122, 17.00, 3);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `reviews`
--

CREATE TABLE `reviews` (
  `review_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `seller_id` int(11) NOT NULL,
  `reservation_id` int(11) NOT NULL,
  `rating` int(1) NOT NULL,
  `comment` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` enum('active','pending_review','deleted') NOT NULL DEFAULT 'active',
  `report_count` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `reviews`
--

INSERT INTO `reviews` (`review_id`, `user_id`, `seller_id`, `reservation_id`, `rating`, `comment`, `created_at`, `status`, `report_count`) VALUES
(65, 38, 14, 137, 5, 'Mera', '2025-04-30 21:40:14', 'deleted', 3),
(66, 38, 14, 135, 4, 'Mer', '2025-04-30 21:40:19', 'active', 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `review_reports`
--

CREATE TABLE `review_reports` (
  `report_id` int(11) NOT NULL,
  `review_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `reason` enum('inappropriate','spam','fraudulent','other') NOT NULL,
  `additional_info` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `review_reports`
--

INSERT INTO `review_reports` (`report_id`, `review_id`, `user_id`, `reason`, `additional_info`, `created_at`) VALUES
(34, 66, 42, 'other', NULL, '2025-05-02 17:12:44');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `sellers`
--

CREATE TABLE `sellers` (
  `seller_id` int(11) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `store_name` varchar(255) DEFAULT NULL,
  `store_description` text DEFAULT NULL,
  `store_phone` varchar(15) DEFAULT NULL,
  `store_address` varchar(255) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `verification_token` varchar(32) DEFAULT NULL,
  `is_verified` tinyint(1) DEFAULT 0,
  `organic_products` tinyint(1) DEFAULT 0,
  `store_type` enum('Supermercados','Mercados Locales','Tiendas Locales') NOT NULL DEFAULT 'Tiendas Locales',
  `store_logo` varchar(255) DEFAULT NULL,
  `opening_time` varchar(5) DEFAULT NULL,
  `closing_time` varchar(5) DEFAULT NULL,
  `store_rating` decimal(3,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `sellers`
--

INSERT INTO `sellers` (`seller_id`, `email`, `password`, `store_name`, `store_description`, `store_phone`, `store_address`, `latitude`, `longitude`, `created_at`, `verification_token`, `is_verified`, `organic_products`, `store_type`, `store_logo`, `opening_time`, `closing_time`, `store_rating`) VALUES
(14, 'naimbadpiggie@gmail.com', '$2y$10$ZHbbQYdNrwMgdih.tJ.vg.DE4ypRC1dofCWX3jsA62SWfx1ksMjiC', 'Abarrotes Don Juan', 'Descripción de los abarrotes jk', '+523331755709', 'Avenida Venustiano Carranza, Zapopan, Jalisco, 45190', 20.726533254510144, -103.3623394129648, '2025-01-28 17:54:02', NULL, 1, 0, 'Tiendas Locales', 'http://10.0.2.2/localfresh/uploads/Abarrotes_Don_Juan/1745720925_cropped_image_1745720921967.jpg', '08:00', '03:00', 4.00),
(16, 'a21300722@ceti.mx', '$2y$10$iR4w5Q8p52sOor.Tz1ecEeDSGacVuHUYps/GiSxkNd5vxenWQAFha', 'Abarrotes De La Esquina', 'Hola', '3331755709', 'C. Rafaél Vega Sánchez, 1200, Zapopan, Jalisco, 45190', 20.727978092303, -103.36091229927, '2025-03-05 19:24:54', NULL, 1, 0, 'Tiendas Locales', 'http://10.0.2.2/localfresh/uploads/Abarrotes_De_La_Esquina/1742586479_compressed_1742586469255.jpg', '00:00', '23:59', 4.10),
(17, 'tienda3@gmail.com', '$2y$10$r1GqAL3UVlPpPHuMO87OB.SdFCjJycYpn78knWXBlR.BlGvrH0Igu', 'Tienda 3', 'Aca', '+523331755701', 'La Paz, Zapopan, Jalisco, 44218', 20.72366186696624, -103.35831430606312, '2025-03-27 19:14:38', 'e39daeefaff0b858451314c234953613', 1, 0, 'Tiendas Locales', 'http://10.0.2.2/localfresh/uploads/Tienda_3/1743125181_compressed_1743125178633.jpg', '10:00', '20:00', 0.00);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `seller_fcm_tokens`
--

CREATE TABLE `seller_fcm_tokens` (
  `id` int(11) NOT NULL,
  `seller_id` int(11) NOT NULL,
  `device_id` varchar(255) NOT NULL,
  `fcm_token` varchar(512) NOT NULL,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `seller_fcm_tokens`
--

INSERT INTO `seller_fcm_tokens` (`id`, `seller_id`, `device_id`, `fcm_token`, `updated_at`) VALUES
(11, 14, '07292917160b59ed', 'cFxfqm08TXawr1zFwJ74K9:APA91bEZbxt3z707ZO2lLf4lmg7qHMWzXu_qi5A2vDr1K8JQEnuKTf-7vllFGP1eXe6Az495h70bwmSsuPqVeu5U9nIt_yxvQ9ygb68S1YDn0JQBvVu0GcM', '2025-04-26 20:40:16'),
(25, 14, '30d47fa5e78ab02f', 'e9HmhEbVRWK7kCXs15LNrc:APA91bHEGwBNzYeSWeUyFIEzVCCYRzeUKS7lWmt1kWVY6bIMzdr7Lvr8RBZlT_mHb3_GwHOl8SdliZMn5YLH3FUf3o1MEzv9jBu1dtt87rSjNwOH5kIiBGo', '2025-04-30 21:39:06'),
(28, 14, '893659bd8ff222a7', 'dNXbtNbMTs6zYivfyHXCyM:APA91bEM6NK81nLR2wd7k8PDB3VKgtT-drLpeeYE69sYia0TWTWtz0L9tk8dxndveb0KWpd3sNT11ISxyN2TgLHJHc5m-plquV00dblw5E-U63rz85TscVw', '2025-05-03 03:32:39');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `seller_notifications`
--

CREATE TABLE `seller_notifications` (
  `notification_id` int(11) NOT NULL,
  `seller_id` int(11) NOT NULL,
  `type` enum('nuevo_apartado','pago','caducado','cancelado','alerta_caducidad','producto_expirado') NOT NULL,
  `title` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`data`)),
  `created_at` datetime DEFAULT current_timestamp(),
  `is_read` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `seller_notifications`
--

INSERT INTO `seller_notifications` (`notification_id`, `seller_id`, `type`, `title`, `message`, `data`, `created_at`, `is_read`) VALUES
(53, 14, 'alerta_caducidad', '¡Alerta! Productos por caducar mañana', 'Productos que caducarán mañana (2025-05-02):\n\n• Sabritas Adobadas - Caduca: 2025-05-09\n\nAcciones recomendadas:\n• Considere aumentar el descuento\n• Coloque en lugar visible\n• Informe a los clientes', '{\"notification_type\":\"alerta_caducidad\",\"products_json\":\"[{\\\"product_id\\\":59,\\\"unit_id\\\":122,\\\"name\\\":\\\"Sabritas Adobadas\\\",\\\"category\\\":\\\"Snacks y botanas\\\",\\\"expiry_date\\\":\\\"2025-05-09\\\",\\\"expiry_type\\\":\\\"consumo_preferente\\\",\\\"quantity\\\":9}]\",\"expiry_date\":\"2025-05-02\",\"products_count\":1,\"recommended_action\":\"Ofrecer descuentos o retirar del inventario\"}', '2025-05-01 13:07:34', 1),
(60, 14, 'producto_expirado', 'Productos expirados en inventario', 'Productos expirados (2025-05-01):\n\n• Arroz integral\n• Arroz integral\n• Doritos Nachos\n• Frijol Negro Valle\n• Frijol Negro Valle\n• Sabritas Adobadas\n• Sobre pedigree \n\nAcción: Estos productos han sido marcados como expirados automáticamente.', '{\"notification_type\":\"producto_expirado\",\"products_json\":\"[{\\\"product_id\\\":55,\\\"unit_id\\\":59,\\\"name\\\":\\\"Arroz integral\\\",\\\"category\\\":\\\"Granos y cereales\\\",\\\"expiry_date\\\":\\\"2025-04-30\\\",\\\"expiry_type\\\":\\\"consumo_preferente\\\",\\\"quantity\\\":5},{\\\"product_id\\\":55,\\\"unit_id\\\":114,\\\"name\\\":\\\"Arroz integral\\\",\\\"category\\\":\\\"Granos y cereales\\\",\\\"expiry_date\\\":\\\"2025-04-30\\\",\\\"expiry_type\\\":\\\"consumo_preferente\\\",\\\"quantity\\\":9},{\\\"product_id\\\":69,\\\"unit_id\\\":131,\\\"name\\\":\\\"Doritos Nachos\\\",\\\"category\\\":\\\"Snacks y botanas\\\",\\\"expiry_date\\\":\\\"2025-04-30\\\",\\\"expiry_type\\\":\\\"consumo_preferente\\\",\\\"quantity\\\":1},{\\\"product_id\\\":56,\\\"unit_id\\\":132,\\\"name\\\":\\\"Frijol Negro Valle\\\",\\\"category\\\":\\\"Legumbres\\\",\\\"expiry_date\\\":\\\"2025-04-27\\\",\\\"expiry_type\\\":\\\"consumo_preferente\\\",\\\"quantity\\\":5},{\\\"product_id\\\":56,\\\"unit_id\\\":133,\\\"name\\\":\\\"Frijol Negro Valle\\\",\\\"category\\\":\\\"Legumbres\\\",\\\"expiry_date\\\":\\\"2025-04-30\\\",\\\"expiry_type\\\":\\\"consumo_preferente\\\",\\\"quantity\\\":3},{\\\"product_id\\\":59,\\\"unit_id\\\":123,\\\"name\\\":\\\"Sabritas Adobadas\\\",\\\"category\\\":\\\"Snacks y botanas\\\",\\\"expiry_date\\\":\\\"2025-04-30\\\",\\\"expiry_type\\\":\\\"consumo_preferente\\\",\\\"quantity\\\":13},{\\\"product_id\\\":61,\\\"unit_id\\\":115,\\\"name\\\":\\\"Sobre pedigree \\\",\\\"category\\\":\\\"Comida para mascotas\\\",\\\"expiry_date\\\":\\\"2025-04-29\\\",\\\"expiry_type\\\":\\\"consumo_preferente\\\",\\\"quantity\\\":10}]\",\"expiry_date\":\"2025-05-01\",\"products_count\":7,\"action_required\":\"Revisar inventario y retirar productos expirados\"}', '2025-05-01 14:25:15', 1),
(61, 14, 'producto_expirado', 'Productos expirados en inventario', 'Productos expirados (2025-05-01):\n\n• Arroz integral\n• Arroz integral\n• Doritos Nachos\n• Frijol Negro Valle\n• Frijol Negro Valle\n• Sabritas Adobadas\n• Sobre pedigree \n\nAcción: Estos productos han sido marcados como expirados automáticamente.', '{\"notification_type\":\"producto_expirado\",\"products_json\":\"[{\\\"product_id\\\":55,\\\"unit_id\\\":59,\\\"name\\\":\\\"Arroz integral\\\",\\\"category\\\":\\\"Granos y cereales\\\",\\\"expiry_date\\\":\\\"2025-04-30\\\",\\\"expiry_type\\\":\\\"consumo_preferente\\\",\\\"quantity\\\":5},{\\\"product_id\\\":55,\\\"unit_id\\\":114,\\\"name\\\":\\\"Arroz integral\\\",\\\"category\\\":\\\"Granos y cereales\\\",\\\"expiry_date\\\":\\\"2025-04-30\\\",\\\"expiry_type\\\":\\\"consumo_preferente\\\",\\\"quantity\\\":9},{\\\"product_id\\\":69,\\\"unit_id\\\":131,\\\"name\\\":\\\"Doritos Nachos\\\",\\\"category\\\":\\\"Snacks y botanas\\\",\\\"expiry_date\\\":\\\"2025-04-30\\\",\\\"expiry_type\\\":\\\"consumo_preferente\\\",\\\"quantity\\\":1},{\\\"product_id\\\":56,\\\"unit_id\\\":132,\\\"name\\\":\\\"Frijol Negro Valle\\\",\\\"category\\\":\\\"Legumbres\\\",\\\"expiry_date\\\":\\\"2025-04-27\\\",\\\"expiry_type\\\":\\\"consumo_preferente\\\",\\\"quantity\\\":5},{\\\"product_id\\\":56,\\\"unit_id\\\":133,\\\"name\\\":\\\"Frijol Negro Valle\\\",\\\"category\\\":\\\"Legumbres\\\",\\\"expiry_date\\\":\\\"2025-04-30\\\",\\\"expiry_type\\\":\\\"consumo_preferente\\\",\\\"quantity\\\":3},{\\\"product_id\\\":59,\\\"unit_id\\\":123,\\\"name\\\":\\\"Sabritas Adobadas\\\",\\\"category\\\":\\\"Snacks y botanas\\\",\\\"expiry_date\\\":\\\"2025-04-30\\\",\\\"expiry_type\\\":\\\"consumo_preferente\\\",\\\"quantity\\\":13},{\\\"product_id\\\":61,\\\"unit_id\\\":115,\\\"name\\\":\\\"Sobre pedigree \\\",\\\"category\\\":\\\"Comida para mascotas\\\",\\\"expiry_date\\\":\\\"2025-04-29\\\",\\\"expiry_type\\\":\\\"consumo_preferente\\\",\\\"quantity\\\":10}]\",\"expiry_date\":\"2025-05-01\",\"products_count\":7,\"action_required\":\"Revisar inventario y retirar productos expirados\"}', '2025-05-01 14:29:40', 1),
(62, 14, 'nuevo_apartado', 'Nuevo apartado recibido', 'El cliente Einar Naim ha realizado un nuevo apartado (ID: 142).', '{\"reservation_id\":\"142\",\"customer_name\":\"Einar Naim\",\"notification_type\":\"nuevo_apartado\"}', '2025-05-01 16:22:44', 1),
(63, 14, 'pago', 'Apartado pagado en tienda', 'El apartado #142 ha sido pagado y recogido por el cliente.', '{\"reservation_id\":142,\"notification_type\":\"pagado\"}', '2025-05-01 16:23:54', 1),
(64, 14, 'nuevo_apartado', 'Nuevo apartado recibido', 'El cliente Einar Naim ha realizado un nuevo apartado (ID: 143).', '{\"reservation_id\":\"143\",\"customer_name\":\"Einar Naim\",\"notification_type\":\"nuevo_apartado\"}', '2025-05-04 14:31:54', 0),
(65, 14, 'cancelado', 'Apartado cancelado', 'El cliente Einar Naim ha cancelado el apartado #143.', '{\"reservation_id\":143,\"customer_name\":\"Einar Naim\",\"notification_type\":\"cancelado\",\"cancel_date\":\"2025-05-04 20:39:54\"}', '2025-05-04 20:39:57', 0),
(66, 14, 'nuevo_apartado', 'Nuevo apartado recibido', 'El cliente Einar Naim ha realizado un nuevo apartado (ID: 144).', '{\"reservation_id\":\"144\",\"customer_name\":\"Einar Naim\",\"notification_type\":\"nuevo_apartado\"}', '2025-05-04 20:40:02', 0),
(67, 14, 'caducado', 'Apartado expirado', 'El apartado #144 ha expirado. Cliente: Einar Naim', '{\"reservation_id\":144,\"customer_name\":\"Einar Naim\",\"notification_type\":\"expirado\"}', '2025-05-04 21:40:09', 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `birthdate` date DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `first_time_login` tinyint(1) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `phone` varchar(15) DEFAULT NULL,
  `verification_token` varchar(255) DEFAULT NULL,
  `is_verified` tinyint(1) DEFAULT 0,
  `username` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `users`
--

INSERT INTO `users` (`user_id`, `birthdate`, `email`, `password`, `first_name`, `first_time_login`, `last_name`, `phone`, `verification_token`, `is_verified`, `username`) VALUES
(38, '2005-01-20', 'einarnaim839@gmail.com', '$2y$10$s4gbb8bVv6pBKo5cWUaFi.opcIZIYSU7W4L90yAaR1IwA3VFMPadS', 'Einar', 0, 'Naim', '3331755709', NULL, 1, 'naimknd'),
(40, '1988-03-27', 'telefono@gmail.com', '$2y$10$8OxrRnzy9427TXc7AcDuze9g1Gg6knMXIfHiwTLeiuiWwA3u7qEM2', 'telefono', 0, 'aca', '+523331755709', '897565bea0d07acfb7222881c9e34e7f', 1, 'tel'),
(41, '2012-04-18', 'wda@gmail.com', '$2y$10$FO4jRP1UgrAscf6URXWhTe0zU.4JnuHKtinaJfQmWVH2tHhKkukWO', 'd', 0, 'awd', '+523331755709', '462f4861d19a6d6ca94b57d689f4fec3', 1, 'awd'),
(42, '2012-04-18', 'dwadsdwd@gmail.com', '$2y$10$f/KBtEdZNQ1Njs40q.Eo9OiTpuJk6Lzov/EcttkKSr78mMGm041we', 'Prueba', 0, 'Validaciones', '3331755709', '460766d01d07bdd9fe310abd1e1d3d61', 1, 'Naim2');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `user_fcm_tokens`
--

CREATE TABLE `user_fcm_tokens` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `fcm_token` varchar(255) NOT NULL,
  `device_id` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `user_fcm_tokens`
--

INSERT INTO `user_fcm_tokens` (`id`, `user_id`, `fcm_token`, `device_id`, `created_at`, `updated_at`) VALUES
(107, 38, 'fetVwH_tQlizk5n7rIoP3l:APA91bFXn51U9CPG05FOLavb2ZAIwsQPrGoj5xokH-JdRFMcrzBZa0gf-Tl_TPk3UA0nr_TckYV-6_3ZEjhIQ4Cqzx6Rtk06lC5Tzj4H25RdLgzE1zsV0Ow', 'ff2217f48e1fffcf', '2025-04-28 03:28:35', '2025-04-28 03:28:35'),
(116, 38, 'dNXbtNbMTs6zYivfyHXCyM:APA91bEM6NK81nLR2wd7k8PDB3VKgtT-drLpeeYE69sYia0TWTWtz0L9tk8dxndveb0KWpd3sNT11ISxyN2TgLHJHc5m-plquV00dblw5E-U63rz85TscVw', '893659bd8ff222a7', '2025-05-02 17:49:50', '2025-05-03 03:34:09'),
(117, 38, 'dkObDZRpR-WQf_RffZYN-v:APA91bHOCR50q1D1ypPtdRWidsc52d5giPbyMdkCMx21430SHLKx3-ktkDYSWv5TlnbU6F7ZCXIUzzD7hMiS1158_je_FCC8GhrDN6zLscxRjA6CwmKEhoE', 'bdd9d4ccbbad2b5e', '2025-05-04 20:31:16', '2025-05-13 22:56:38');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `user_notification_preferences`
--

CREATE TABLE `user_notification_preferences` (
  `user_id` int(11) NOT NULL,
  `frequency` enum('diaria','semanal','ofertas_especiales','sin_restriccion') NOT NULL DEFAULT 'sin_restriccion',
  `notif_ofertas` tinyint(1) DEFAULT 1,
  `notif_nuevos_productos` tinyint(1) DEFAULT 1,
  `notif_recordatorios` tinyint(1) DEFAULT 1,
  `last_ofertas_sent` datetime DEFAULT NULL,
  `last_nuevos_productos_sent` datetime DEFAULT NULL,
  `last_recordatorios_sent` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `user_notification_preferences`
--

INSERT INTO `user_notification_preferences` (`user_id`, `frequency`, `notif_ofertas`, `notif_nuevos_productos`, `notif_recordatorios`, `last_ofertas_sent`, `last_nuevos_productos_sent`, `last_recordatorios_sent`) VALUES
(38, 'sin_restriccion', 1, 1, 1, '2025-05-04 14:32:58', '2025-04-23 23:44:03', NULL),
(42, 'sin_restriccion', 1, 1, 1, '2025-04-24 00:19:45', NULL, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `user_preferences`
--

CREATE TABLE `user_preferences` (
  `preference_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `preferencias_bebidas` tinyint(1) DEFAULT NULL,
  `preferencias_carnes` tinyint(1) DEFAULT NULL,
  `preferencias_comida_mascotas` tinyint(1) DEFAULT NULL,
  `preferencias_congelados` tinyint(1) DEFAULT NULL,
  `preferencias_granos_cereales` tinyint(1) DEFAULT NULL,
  `preferencias_huevos` tinyint(1) DEFAULT NULL,
  `preferencias_lacteos` tinyint(1) DEFAULT NULL,
  `preferencias_legumbres` tinyint(1) DEFAULT NULL,
  `preferencias_panaderia` tinyint(1) DEFAULT NULL,
  `preferencias_snacks` tinyint(1) DEFAULT NULL,
  `rango_distancia` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `user_preferences`
--

INSERT INTO `user_preferences` (`preference_id`, `user_id`, `preferencias_bebidas`, `preferencias_carnes`, `preferencias_comida_mascotas`, `preferencias_congelados`, `preferencias_granos_cereales`, `preferencias_huevos`, `preferencias_lacteos`, `preferencias_legumbres`, `preferencias_panaderia`, `preferencias_snacks`, `rango_distancia`) VALUES
(18, 38, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3),
(20, 40, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 2),
(21, 41, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 4),
(28, 42, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `user_tokens`
--

CREATE TABLE `user_tokens` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `user_type` varchar(50) NOT NULL,
  `token` varchar(512) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `device_id` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `user_tokens`
--

INSERT INTO `user_tokens` (`id`, `user_id`, `user_type`, `token`, `created_at`, `device_id`) VALUES
(134, 38, 'user', 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vMTI3LjAuMC4xIiwiYXVkIjoiaHR0cDovLzEyNy4wLjAuMSIsImlhdCI6MTc0MTkwMzgwOCwibmJmIjoxNzQxOTAzODA4LCJkYXRhIjp7ImlkIjozOCwiZW1haWwiOiJlaW5hcm5haW04MzlAZ21haWwuY29tIiwidXNlclR5cGUiOiJ1c2VyIn19.x41nyC3Q3EH_K8Rc-CW0f0JV7qsluXrxDpbv5ofpXoIa', '2025-03-13 00:12:35', '6b3a7950a36a537e'),
(264, 38, 'user', 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vMTI3LjAuMC4xIiwiYXVkIjoiaHR0cDovLzEyNy4wLjAuMSIsImlhdCI6MTc0NTM4NzEzNywibmJmIjoxNzQ1Mzg3MTM3LCJkYXRhIjp7ImlkIjozOCwiZW1haWwiOiJlaW5hcm5haW04MzlAZ21haWwuY29tIiwidXNlclR5cGUiOiJ1c2VyIn19.XFPTn88RQz_UkUTJXfiq4M819_EjO6_W0Jvfj3eH4VM', '2025-04-23 05:45:37', '2a905ebd426ecd0a'),
(266, 14, 'seller', 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vMTI3LjAuMC4xIiwiYXVkIjoiaHR0cDovLzEyNy4wLjAuMSIsImlhdCI6MTc0NTQ0OTExMiwibmJmIjoxNzQ1NDQ5MTEyLCJkYXRhIjp7ImlkIjoxNCwiZW1haWwiOiJuYWltYmFkcGlnZ2llQGdtYWlsLmNvbSIsInVzZXJUeXBlIjoic2VsbGVyIn19.OQ42NFpTyCzTSkkFaitJSsedb9lrqG_f5XphJr-rbLM', '2025-04-23 22:57:10', 'ac36cf1efa6a1b2b'),
(272, 38, 'user', 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vMTI3LjAuMC4xIiwiYXVkIjoiaHR0cDovLzEyNy4wLjAuMSIsImlhdCI6MTc0NTQ0OTgxNCwibmJmIjoxNzQ1NDQ5ODE0LCJkYXRhIjp7ImlkIjozOCwiZW1haWwiOiJlaW5hcm5haW04MzlAZ21haWwuY29tIiwidXNlclR5cGUiOiJ1c2VyIn19.muSRvXlkgB5ws-3PyODhfrDGU80BESsrmuHFUFbJsj8', '2025-04-23 23:10:14', 'ac36cf1efa6a1b2b'),
(273, 14, 'seller', 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vMTI3LjAuMC4xIiwiYXVkIjoiaHR0cDovLzEyNy4wLjAuMSIsImlhdCI6MTc0NTQ1MTU5NCwibmJmIjoxNzQ1NDUxNTk0LCJkYXRhIjp7ImlkIjoxNCwiZW1haWwiOiJuYWltYmFkcGlnZ2llQGdtYWlsLmNvbSIsInVzZXJUeXBlIjoic2VsbGVyIn19.NaIDnuQpZnu2zpm9fR1CVsCL3HkvYyfDhia6PiwUatM', '2025-04-23 23:12:20', 'ABC123XYZ'),
(298, 14, 'seller', 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vMTI3LjAuMC4xIiwiYXVkIjoiaHR0cDovLzEyNy4wLjAuMSIsImlhdCI6MTc0NTcwMDAxNiwibmJmIjoxNzQ1NzAwMDE2LCJkYXRhIjp7ImlkIjoxNCwiZW1haWwiOiJuYWltYmFkcGlnZ2llQGdtYWlsLmNvbSIsInVzZXJUeXBlIjoic2VsbGVyIn19.eS3D6Rtfi-oAIAIb4pFR1shhNbEJVHsq8gdKr5_kBDc', '2025-04-26 20:40:16', '07292917160b59ed'),
(315, 38, 'user', 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vMTI3LjAuMC4xIiwiYXVkIjoiaHR0cDovLzEyNy4wLjAuMSIsImlhdCI6MTc0NTgxMDkxNSwibmJmIjoxNzQ1ODEwOTE1LCJkYXRhIjp7ImlkIjozOCwiZW1haWwiOiJlaW5hcm5haW04MzlAZ21haWwuY29tIiwidXNlclR5cGUiOiJ1c2VyIn19.f3Ee1j75PiEYzKRBTH4bmJYUvVkhfnbYwZRg4GI1Nhg', '2025-04-28 03:28:35', 'ff2217f48e1fffcf'),
(328, 14, 'seller', 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vMTI3LjAuMC4xIiwiYXVkIjoiaHR0cDovLzEyNy4wLjAuMSIsImlhdCI6MTc0NjA0OTE0NiwibmJmIjoxNzQ2MDQ5MTQ2LCJkYXRhIjp7ImlkIjoxNCwiZW1haWwiOiJuYWltYmFkcGlnZ2llQGdtYWlsLmNvbSIsInVzZXJUeXBlIjoic2VsbGVyIn19.NXFnowZZbxfSsFWpGQ2vMEOe2NDYeCU-jibJy2BRTJE', '2025-04-30 21:39:06', '30d47fa5e78ab02f'),
(332, 38, 'user', 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vMTI3LjAuMC4xIiwiYXVkIjoiaHR0cDovLzEyNy4wLjAuMSIsImlhdCI6MTc0NjI0MzI0OSwibmJmIjoxNzQ2MjQzMjQ5LCJkYXRhIjp7ImlkIjozOCwiZW1haWwiOiJlaW5hcm5haW04MzlAZ21haWwuY29tIiwidXNlclR5cGUiOiJ1c2VyIn19.lhcGD7bYgISWoIlDRlL4DpmDZKuLoGm3zCF-fmwwu90', '2025-05-02 17:49:50', '893659bd8ff222a7'),
(334, 14, 'seller', 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vMTI3LjAuMC4xIiwiYXVkIjoiaHR0cDovLzEyNy4wLjAuMSIsImlhdCI6MTc0NjI0MzE1OSwibmJmIjoxNzQ2MjQzMTU5LCJkYXRhIjp7ImlkIjoxNCwiZW1haWwiOiJuYWltYmFkcGlnZ2llQGdtYWlsLmNvbSIsInVzZXJUeXBlIjoic2VsbGVyIn19.SBCo-NF5EmZGkSPXLvBuRENxJMCw7-LA8UH1TaveoFA', '2025-05-03 03:32:39', '893659bd8ff222a7'),
(335, 38, 'user', 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vMTI3LjAuMC4xIiwiYXVkIjoiaHR0cDovLzEyNy4wLjAuMSIsImlhdCI6MTc0NzE3Njk5OCwibmJmIjoxNzQ3MTc2OTk4LCJkYXRhIjp7ImlkIjozOCwiZW1haWwiOiJlaW5hcm5haW04MzlAZ21haWwuY29tIiwidXNlclR5cGUiOiJ1c2VyIn19.97Tb-8kMBk_whwFt7aaRu-gU28dDzLlynZxR2KMfwbE', '2025-05-04 20:31:16', 'bdd9d4ccbbad2b5e');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `admin_notifications`
--
ALTER TABLE `admin_notifications`
  ADD PRIMARY KEY (`notification_id`);

--
-- Indices de la tabla `carts`
--
ALTER TABLE `carts`
  ADD PRIMARY KEY (`cart_id`),
  ADD KEY `seller_id` (`seller_id`),
  ADD KEY `idx_cart_expiration` (`expiration_time`),
  ADD KEY `idx_cart_user` (`user_id`);

--
-- Indices de la tabla `cart_items`
--
ALTER TABLE `cart_items`
  ADD PRIMARY KEY (`item_id`),
  ADD KEY `cart_id` (`cart_id`),
  ADD KEY `unit_id` (`unit_id`);

--
-- Indices de la tabla `favorite_products`
--
ALTER TABLE `favorite_products`
  ADD PRIMARY KEY (`favorite_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `product_id` (`product_id`);

--
-- Indices de la tabla `favorite_stores`
--
ALTER TABLE `favorite_stores`
  ADD PRIMARY KEY (`favorite_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `seller_id` (`seller_id`);

--
-- Indices de la tabla `password_resets`
--
ALTER TABLE `password_resets`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`product_id`),
  ADD KEY `seller_id` (`seller_id`);

--
-- Indices de la tabla `product_units`
--
ALTER TABLE `product_units`
  ADD PRIMARY KEY (`unit_id`),
  ADD KEY `product_id` (`product_id`);

--
-- Indices de la tabla `reservations`
--
ALTER TABLE `reservations`
  ADD PRIMARY KEY (`reservation_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `seller_id` (`seller_id`);

--
-- Indices de la tabla `reservation_units`
--
ALTER TABLE `reservation_units`
  ADD PRIMARY KEY (`reservation_unit_id`),
  ADD KEY `reservation_id` (`reservation_id`),
  ADD KEY `unit_id` (`unit_id`);

--
-- Indices de la tabla `reviews`
--
ALTER TABLE `reviews`
  ADD PRIMARY KEY (`review_id`),
  ADD UNIQUE KEY `user_reservation` (`user_id`,`reservation_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `seller_id` (`seller_id`),
  ADD KEY `reservation_id` (`reservation_id`);

--
-- Indices de la tabla `review_reports`
--
ALTER TABLE `review_reports`
  ADD PRIMARY KEY (`report_id`),
  ADD UNIQUE KEY `unique_report` (`review_id`,`user_id`),
  ADD KEY `review_id` (`review_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indices de la tabla `sellers`
--
ALTER TABLE `sellers`
  ADD PRIMARY KEY (`seller_id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indices de la tabla `seller_fcm_tokens`
--
ALTER TABLE `seller_fcm_tokens`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_seller_device` (`seller_id`,`device_id`);

--
-- Indices de la tabla `seller_notifications`
--
ALTER TABLE `seller_notifications`
  ADD PRIMARY KEY (`notification_id`),
  ADD KEY `seller_id` (`seller_id`);

--
-- Indices de la tabla `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `unique_email` (`email`);

--
-- Indices de la tabla `user_fcm_tokens`
--
ALTER TABLE `user_fcm_tokens`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_user_device` (`user_id`,`device_id`);

--
-- Indices de la tabla `user_notification_preferences`
--
ALTER TABLE `user_notification_preferences`
  ADD PRIMARY KEY (`user_id`);

--
-- Indices de la tabla `user_preferences`
--
ALTER TABLE `user_preferences`
  ADD PRIMARY KEY (`preference_id`),
  ADD UNIQUE KEY `user_id_2` (`user_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indices de la tabla `user_tokens`
--
ALTER TABLE `user_tokens`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `admin_notifications`
--
ALTER TABLE `admin_notifications`
  MODIFY `notification_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT de la tabla `carts`
--
ALTER TABLE `carts`
  MODIFY `cart_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=245;

--
-- AUTO_INCREMENT de la tabla `cart_items`
--
ALTER TABLE `cart_items`
  MODIFY `item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=357;

--
-- AUTO_INCREMENT de la tabla `favorite_products`
--
ALTER TABLE `favorite_products`
  MODIFY `favorite_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=49;

--
-- AUTO_INCREMENT de la tabla `favorite_stores`
--
ALTER TABLE `favorite_stores`
  MODIFY `favorite_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=53;

--
-- AUTO_INCREMENT de la tabla `password_resets`
--
ALTER TABLE `password_resets`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=56;

--
-- AUTO_INCREMENT de la tabla `products`
--
ALTER TABLE `products`
  MODIFY `product_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=80;

--
-- AUTO_INCREMENT de la tabla `product_units`
--
ALTER TABLE `product_units`
  MODIFY `unit_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=135;

--
-- AUTO_INCREMENT de la tabla `reservations`
--
ALTER TABLE `reservations`
  MODIFY `reservation_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=145;

--
-- AUTO_INCREMENT de la tabla `reservation_units`
--
ALTER TABLE `reservation_units`
  MODIFY `reservation_unit_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=193;

--
-- AUTO_INCREMENT de la tabla `reviews`
--
ALTER TABLE `reviews`
  MODIFY `review_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=67;

--
-- AUTO_INCREMENT de la tabla `review_reports`
--
ALTER TABLE `review_reports`
  MODIFY `report_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=35;

--
-- AUTO_INCREMENT de la tabla `sellers`
--
ALTER TABLE `sellers`
  MODIFY `seller_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT de la tabla `seller_fcm_tokens`
--
ALTER TABLE `seller_fcm_tokens`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT de la tabla `seller_notifications`
--
ALTER TABLE `seller_notifications`
  MODIFY `notification_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=68;

--
-- AUTO_INCREMENT de la tabla `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=43;

--
-- AUTO_INCREMENT de la tabla `user_fcm_tokens`
--
ALTER TABLE `user_fcm_tokens`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=118;

--
-- AUTO_INCREMENT de la tabla `user_preferences`
--
ALTER TABLE `user_preferences`
  MODIFY `preference_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=51;

--
-- AUTO_INCREMENT de la tabla `user_tokens`
--
ALTER TABLE `user_tokens`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=336;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `carts`
--
ALTER TABLE `carts`
  ADD CONSTRAINT `carts_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  ADD CONSTRAINT `carts_ibfk_2` FOREIGN KEY (`seller_id`) REFERENCES `sellers` (`seller_id`);

--
-- Filtros para la tabla `cart_items`
--
ALTER TABLE `cart_items`
  ADD CONSTRAINT `cart_items_new_ibfk_1` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`cart_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `cart_items_new_ibfk_2` FOREIGN KEY (`unit_id`) REFERENCES `product_units` (`unit_id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `favorite_products`
--
ALTER TABLE `favorite_products`
  ADD CONSTRAINT `favorite_products_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `favorite_products_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `favorite_stores`
--
ALTER TABLE `favorite_stores`
  ADD CONSTRAINT `favorite_stores_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `favorite_stores_ibfk_2` FOREIGN KEY (`seller_id`) REFERENCES `sellers` (`seller_id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `products`
--
ALTER TABLE `products`
  ADD CONSTRAINT `products_ibfk_1` FOREIGN KEY (`seller_id`) REFERENCES `sellers` (`seller_id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `product_units`
--
ALTER TABLE `product_units`
  ADD CONSTRAINT `product_units_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `reservations`
--
ALTER TABLE `reservations`
  ADD CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `reservations_ibfk_2` FOREIGN KEY (`seller_id`) REFERENCES `sellers` (`seller_id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `reservation_units`
--
ALTER TABLE `reservation_units`
  ADD CONSTRAINT `reservation_units_ibfk_1` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`reservation_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `reservation_units_ibfk_2` FOREIGN KEY (`unit_id`) REFERENCES `product_units` (`unit_id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `reviews`
--
ALTER TABLE `reviews`
  ADD CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `reviews_ibfk_2` FOREIGN KEY (`seller_id`) REFERENCES `sellers` (`seller_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `reviews_ibfk_3` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`reservation_id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `review_reports`
--
ALTER TABLE `review_reports`
  ADD CONSTRAINT `review_reports_ibfk_1` FOREIGN KEY (`review_id`) REFERENCES `reviews` (`review_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `review_reports_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `seller_fcm_tokens`
--
ALTER TABLE `seller_fcm_tokens`
  ADD CONSTRAINT `seller_fcm_tokens_ibfk_1` FOREIGN KEY (`seller_id`) REFERENCES `sellers` (`seller_id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `seller_notifications`
--
ALTER TABLE `seller_notifications`
  ADD CONSTRAINT `seller_notifications_ibfk_1` FOREIGN KEY (`seller_id`) REFERENCES `sellers` (`seller_id`);

--
-- Filtros para la tabla `user_fcm_tokens`
--
ALTER TABLE `user_fcm_tokens`
  ADD CONSTRAINT `user_fcm_tokens_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `user_notification_preferences`
--
ALTER TABLE `user_notification_preferences`
  ADD CONSTRAINT `user_notification_preferences_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `user_preferences`
--
ALTER TABLE `user_preferences`
  ADD CONSTRAINT `user_preferences_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
