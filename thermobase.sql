-- phpMyAdmin SQL Dump
-- version 4.8.3
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Jun 26, 2020 at 05:59 AM
-- Server version: 5.6.47-cll-lve
-- PHP Version: 7.2.7

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `thermobase`
--

-- --------------------------------------------------------

--
-- Table structure for table `CHANNELS`
--

CREATE TABLE `CHANNELS` (
  `CHANNEL_ID` bigint(20) UNSIGNED NOT NULL,
  `GUILD_ID` bigint(20) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `CHANNEL_SETTINGS`
--

CREATE TABLE `CHANNEL_SETTINGS` (
  `CHANNEL_ID` bigint(20) UNSIGNED NOT NULL,
  `MIN_SLOW` int(11) DEFAULT NULL,
  `MAX_SLOW` int(11) DEFAULT NULL,
  `MONITORED` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `GUILDS`
--

CREATE TABLE `GUILDS` (
  `GUILD_ID` bigint(20) UNSIGNED NOT NULL,
  `GUILD_ENABLE` tinyint(1) NOT NULL,
  `GUILD_PREFIX` varchar(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `CHANNELS`
--
ALTER TABLE `CHANNELS`
  ADD PRIMARY KEY (`CHANNEL_ID`),
  ADD KEY `GUILD_ID` (`GUILD_ID`);

--
-- Indexes for table `CHANNEL_SETTINGS`
--
ALTER TABLE `CHANNEL_SETTINGS`
  ADD PRIMARY KEY (`CHANNEL_ID`);

--
-- Indexes for table `GUILDS`
--
ALTER TABLE `GUILDS`
  ADD PRIMARY KEY (`GUILD_ID`),
  ADD UNIQUE KEY `GUILD_ID` (`GUILD_ID`);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `CHANNELS`
--
ALTER TABLE `CHANNELS`
  ADD CONSTRAINT `CHANNELS_ibfk_1` FOREIGN KEY (`GUILD_ID`) REFERENCES `GUILDS` (`GUILD_ID`);

--
-- Constraints for table `CHANNEL_SETTINGS`
--
ALTER TABLE `CHANNEL_SETTINGS`
  ADD CONSTRAINT `CHANNEL_SETTINGS_ibfk_1` FOREIGN KEY (`CHANNEL_ID`) REFERENCES `CHANNELS` (`CHANNEL_ID`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
