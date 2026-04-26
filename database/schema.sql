-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: stationery_billing
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bill_items`
--

DROP TABLE IF EXISTS `bill_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `bill_id` int NOT NULL,
  `product_id` int NOT NULL,
  `product_name` varchar(100) NOT NULL,
  `quantity` int NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `bill_id` (`bill_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `bill_items_ibfk_1` FOREIGN KEY (`bill_id`) REFERENCES `bills` (`id`) ON DELETE CASCADE,
  CONSTRAINT `bill_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bill_items`
--

LOCK TABLES `bill_items` WRITE;
/*!40000 ALTER TABLE `bill_items` DISABLE KEYS */;
/*!40000 ALTER TABLE `bill_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bills`
--

DROP TABLE IF EXISTS `bills`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bills` (
  `id` int NOT NULL AUTO_INCREMENT,
  `bill_number` varchar(20) NOT NULL,
  `bill_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `total_amount` decimal(10,2) NOT NULL,
  `status` enum('PENDING','PAID','FAILED','EXPIRED') DEFAULT 'PENDING',
  `payment_date` timestamp NULL DEFAULT NULL,
  `qr_code` text,
  `qr_expiry` timestamp NULL DEFAULT NULL,
  `cancelled_at` timestamp NULL DEFAULT NULL,
  `expired_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `bill_number` (`bill_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bills`
--

LOCK TABLES `bills` WRITE;
/*!40000 ALTER TABLE `bills` DISABLE KEYS */;
/*!40000 ALTER TABLE `bills` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `description` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'Writing Instruments','Pens, pencils, markers, and highlighters','2026-04-25 19:58:14'),(2,'Paper Products','Notebooks, sheets, notes, and drawing books','2026-04-25 19:58:14'),(3,'Office Supplies','Files, folders, staplers, and clips','2026-04-25 19:58:14'),(4,'Art & Craft Supplies','Crayons, colors, paints, and scissors','2026-04-25 19:58:14'),(5,'Geometry & Tools','Scales, compass, boxes, and erasers','2026-04-25 19:58:14');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `category_id` int NOT NULL,
  `description` text,
  `price` decimal(10,2) NOT NULL,
  `stock_quantity` int DEFAULT '0',
  `reorder_level` int DEFAULT '10',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'Pen',1,'Classic blue ink ballpoint pen',15.00,200,20,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(2,'Pencil',1,'HB wooden pencil with eraser',10.00,150,25,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(3,'Marker',1,'Black permanent marker',35.00,80,10,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(4,'Highlighter',1,'Yellow fluorescent highlighter',40.00,60,10,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(5,'Gel Pen',1,'Smooth black gel pen',25.00,120,15,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(6,'Mechanical Pencil',1,'0.5mm automatic pencil',50.00,50,8,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(7,'Notebook',2,'100 pages ruled notebook',45.00,100,15,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(8,'A4 Sheets',2,'Pack of 100 A4 printing sheets',120.00,40,5,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(9,'Sticky Notes',2,'3x3 inch pad of 100 sheets',60.00,70,10,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(10,'Drawing Book',2,'40 pages thick drawing paper',80.00,30,5,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(11,'Notepad',2,'A5 spiral notepad',55.00,60,8,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(12,'Chart Paper',2,'Colorful presentation chart (single)',25.00,90,15,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(13,'File',3,'Cardboard file folder (single)',30.00,100,20,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(14,'Folder',3,'Plastic transparent folder',25.00,120,15,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(15,'Stapler',3,'Standard 20-sheet stapler',120.00,30,5,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(16,'Punch Machine',3,'Single hole paper punch',90.00,25,5,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(17,'Paper Clips',3,'Box of 100 paper clips',20.00,150,20,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(18,'Envelope',3,'A4 size envelope (pack of 10)',40.00,80,10,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(19,'Crayons',4,'12 color non-toxic crayons',65.00,45,8,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(20,'Color Pencils',4,'12 shade color pencil set',85.00,40,8,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(21,'Paints',4,'12 color watercolor paint set',150.00,25,5,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(22,'Glue',4,'50ml white craft glue',35.00,60,10,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(23,'Scissors',4,'Safety scissors for kids',70.00,35,5,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(24,'Sketch Pens',4,'10 color sketch pen set',95.00,40,6,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(25,'Scale',5,'15cm plastic ruler',15.00,100,20,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(26,'Compass',5,'Metal geometry compass',45.00,50,8,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(27,'Protractor',5,'180 degree semicircular protractor',25.00,60,10,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(28,'Geometry Box',5,'Complete set (compass, scale, protractor, set squares)',150.00,30,5,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(29,'Eraser',5,'Soft PVC eraser',10.00,200,30,'2026-04-25 19:58:14','2026-04-25 19:58:14'),(30,'Sharpener',5,'Metal pencil sharpener',15.00,150,25,'2026-04-25 19:58:14','2026-04-25 19:58:14');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock_logs`
--

DROP TABLE IF EXISTS `stock_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_logs` (
  `id` int NOT NULL AUTO_INCREMENT,
  `product_id` int NOT NULL,
  `bill_id` int DEFAULT NULL,
  `quantity_change` int NOT NULL,
  `stock_before` int NOT NULL,
  `stock_after` int NOT NULL,
  `reason` enum('SALE','RESTOCK','CANCELLATION','EXPIRY_RESTORE') NOT NULL,
  `log_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `product_id` (`product_id`),
  KEY `bill_id` (`bill_id`),
  CONSTRAINT `stock_logs_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `stock_logs_ibfk_2` FOREIGN KEY (`bill_id`) REFERENCES `bills` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_logs`
--

LOCK TABLES `stock_logs` WRITE;
/*!40000 ALTER TABLE `stock_logs` DISABLE KEYS */;
/*!40000 ALTER TABLE `stock_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(20) DEFAULT 'ADMIN',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','admin123','ADMIN','2026-04-25 19:58:14');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-26 11:17:48
