USE `sipesca`;
DROP procedure IF EXISTS `agrupaPasosPorIntervalosNodosSeparados`;

DELIMITER $$
USE `sipesca`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `agrupaPasosPorIntervalosNodosSeparados`(in fechaMIN Varchar(40), in fechaMAX Varchar(40), in intervalo INT)
BEGIN
DECLARE inter INT DEFAULT intervalo*1000*60;
DECLARE corte INT DEFAULT 19;

IF intervalo >= 1440 THEN SET corte = 10; END IF;

SELECT SUBSTR(FROM_UNIXTIME(truncate(tinicio/inter,0)*inter/1000),1,corte) as Fecha, idNodo ,  count(*)
	FROM paso
		WHERE
			tinicio
			BETWEEN  UNIX_TIMESTAMP(fechaMIN)*1000
				AND  UNIX_TIMESTAMP(fechaMAX)*1000
		
	GROUP BY
		idNodo,
		truncate(tinicio/inter,0)
	ORDER BY Fecha;
 
END$$

DELIMITER ;