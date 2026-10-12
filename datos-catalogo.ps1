# pcforge-datos-catalogo.ps1
# Fuente unica de verdad del catalogo de demostracion de PCForge.
#
# De aqui se generan las dos cosas que tienen que coincidir:
#   - el CatalogSeeder.java (datos para una base nueva)
#   - el script de migracion por API (datos para la base que ya existe)
#
# Campos:
#   viejo  = SKU con el que el producto existe hoy en la base (null si es nuevo)
#   sku    = SKU nuevo
#   precio = en pesos colombianos, valor ilustrativo de demostracion
#   specs  = las que usa el motor de compatibilidad; extra = ficha informativa

function Get-CatalogoPCForge {

    $procesadores = @(
        @{ viejo='CPU-AM4-5700'; sku='CPU-RYZEN7-5700X'; name='AMD Ryzen 7 5700X'; brand='AMD'; cat='procesadores'; tipo='PROCESADOR'
           desc='Procesador de 8 nucleos para socket AM4, con 16 hilos y 32 MB de cache L3. La opcion AM4 mas equilibrada del catalogo.'
           price=629000; stock=5; min=2; socket='AM4'; powerW=65
           extra=[ordered]@{ 'Nucleos'='8'; 'Hilos'='16'; 'Frecuencia base'='3.4 GHz'; 'Frecuencia turbo'='4.6 GHz'; 'Cache L3'='32 MB'; 'Arquitectura'='Zen 3' } }
        @{ viejo=$null; sku='CPU-RYZEN5-5600X'; name='AMD Ryzen 5 5600X'; brand='AMD'; cat='procesadores'; tipo='PROCESADOR'
           desc='Procesador de 6 nucleos y 12 hilos para socket AM4. Consumo bajo y buen rendimiento por peso, ideal para equipos de entrada.'
           price=489000; stock=8; min=3; socket='AM4'; powerW=65
           extra=[ordered]@{ 'Nucleos'='6'; 'Hilos'='12'; 'Frecuencia base'='3.7 GHz'; 'Frecuencia turbo'='4.6 GHz'; 'Cache L3'='32 MB'; 'Arquitectura'='Zen 3' } }
        @{ viejo='CPU-AM5-7600'; sku='CPU-RYZEN5-7600X'; name='AMD Ryzen 5 7600X'; brand='AMD'; cat='procesadores'; tipo='PROCESADOR'
           desc='Procesador de 6 nucleos y 12 hilos para socket AM5, la plataforma vigente de AMD. Incluye graficos integrados.'
           price=899000; stock=7; min=2; socket='AM5'; powerW=105
           extra=[ordered]@{ 'Nucleos'='6'; 'Hilos'='12'; 'Frecuencia base'='4.7 GHz'; 'Frecuencia turbo'='5.3 GHz'; 'Cache L3'='32 MB'; 'Graficos integrados'='Si'; 'Arquitectura'='Zen 4' } }
        @{ viejo='CPU-AM5-8600'; sku='CPU-RYZEN7-7800X3D'; name='AMD Ryzen 7 7800X3D'; brand='AMD'; cat='procesadores'; tipo='PROCESADOR'
           desc='Procesador de 8 nucleos y 16 hilos con 96 MB de cache 3D V-Cache. Referente para juego de alto rendimiento.'
           price=1890000; stock=5; min=1; socket='AM5'; powerW=120
           extra=[ordered]@{ 'Nucleos'='8'; 'Hilos'='16'; 'Frecuencia base'='4.2 GHz'; 'Frecuencia turbo'='5.0 GHz'; 'Cache L3'='96 MB'; 'Graficos integrados'='Si'; 'Arquitectura'='Zen 4' } }
        @{ viejo=$null; sku='CPU-RYZEN9-7950X'; name='AMD Ryzen 9 7950X'; brand='AMD'; cat='procesadores'; tipo='PROCESADOR'
           desc='Procesador de 16 nucleos y 32 hilos para socket AM5. Pensado para edicion, renderizado y cargas de trabajo pesadas.'
           price=2450000; stock=4; min=1; socket='AM5'; powerW=170
           extra=[ordered]@{ 'Nucleos'='16'; 'Hilos'='32'; 'Frecuencia base'='4.5 GHz'; 'Frecuencia turbo'='5.7 GHz'; 'Cache L3'='64 MB'; 'Arquitectura'='Zen 4' } }
        @{ viejo=$null; sku='CPU-RYZEN7-9700X'; name='AMD Ryzen 7 9700X'; brand='AMD'; cat='procesadores'; tipo='PROCESADOR'
           desc='Procesador de 8 nucleos y 16 hilos de la generacion mas reciente para socket AM5, con solo 65 W de consumo.'
           price=1690000; stock=5; min=1; socket='AM5'; powerW=65
           extra=[ordered]@{ 'Nucleos'='8'; 'Hilos'='16'; 'Frecuencia base'='3.8 GHz'; 'Frecuencia turbo'='5.5 GHz'; 'Cache L3'='32 MB'; 'Arquitectura'='Zen 5' } }
        @{ viejo='CPU-LGA1700-12400'; sku='CPU-I5-12400F'; name='Intel Core i5-12400F'; brand='Intel'; cat='procesadores'; tipo='PROCESADOR'
           desc='Procesador de 6 nucleos y 12 hilos para socket LGA1700, sin graficos integrados. La opcion economica de Intel en el catalogo.'
           price=519000; stock=9; min=3; socket='LGA1700'; powerW=65
           extra=[ordered]@{ 'Nucleos'='6'; 'Hilos'='12'; 'Frecuencia base'='2.5 GHz'; 'Frecuencia turbo'='4.4 GHz'; 'Cache L3'='18 MB'; 'Graficos integrados'='No'; 'Arquitectura'='Alder Lake' } }
        @{ viejo='CPU-LGA1700-1470'; sku='CPU-I7-12700K'; name='Intel Core i7-12700K'; brand='Intel'; cat='procesadores'; tipo='PROCESADOR'
           desc='Procesador de 12 nucleos (8 de rendimiento y 4 de eficiencia) y 20 hilos para socket LGA1700. Desbloqueado para overclock.'
           price=1450000; stock=5; min=1; socket='LGA1700'; powerW=190
           extra=[ordered]@{ 'Nucleos'='12 (8P + 4E)'; 'Hilos'='20'; 'Frecuencia turbo'='5.0 GHz'; 'Cache L3'='25 MB'; 'Graficos integrados'='Si'; 'Arquitectura'='Alder Lake' } }
        @{ viejo=$null; sku='CPU-I9-14900K'; name='Intel Core i9-14900K'; brand='Intel'; cat='procesadores'; tipo='PROCESADOR'
           desc='Procesador de 24 nucleos (8 de rendimiento y 16 de eficiencia) y 32 hilos para socket LGA1700. Consumo muy alto: exige refrigeracion y fuente acordes.'
           price=2790000; stock=3; min=1; socket='LGA1700'; powerW=253
           extra=[ordered]@{ 'Nucleos'='24 (8P + 16E)'; 'Hilos'='32'; 'Frecuencia turbo'='6.0 GHz'; 'Cache L3'='36 MB'; 'Graficos integrados'='Si'; 'Arquitectura'='Raptor Lake Refresh' } }
        @{ viejo='CPU-LGA1851-2450'; sku='CPU-ULTRA7-265K'; name='Intel Core Ultra 7 265K'; brand='Intel'; cat='procesadores'; tipo='PROCESADOR'
           desc='Procesador de 20 nucleos y 20 hilos para el socket LGA1851 con memoria DDR5. Incluye unidad de procesamiento neuronal.'
           price=1790000; stock=4; min=1; socket='LGA1851'; powerW=125
           extra=[ordered]@{ 'Nucleos'='20 (8P + 12E)'; 'Hilos'='20'; 'Frecuencia turbo'='5.5 GHz'; 'Cache L3'='30 MB'; 'Graficos integrados'='Si'; 'Arquitectura'='Arrow Lake' } }
        @{ viejo='CPU-OEM-IND'; sku='CPU-OEM-SIN-FICHA'; name='Procesador OEM sin ficha tecnica publicada'; brand='Generica'; cat='procesadores'; tipo='PROCESADOR'
           desc='Producto de prueba que no declara socket. Sirve para verificar que el validador avisa en lugar de afirmar una compatibilidad que no puede comprobar.'
           price=399000; stock=3; min=1; socket=$null; powerW=65
           extra=[ordered]@{ 'Nota'='Sin ficha tecnica del fabricante' } }
    )

    $placas = @(
        @{ viejo='MB-AM5-DDR5'; sku='MB-B650-P-WIFI'; name='MSI PRO B650-P WIFI'; brand='MSI'; cat='placas-base'; tipo='PLACA_BASE'
           desc='Placa base ATX para socket AM5 con memoria DDR5, red WiFi 6E, dos ranuras M.2 y disipadores integrados.'
           price=895000; stock=6; min=2; socket='AM5'; ramSup=@('DDR5'); form='ATX'
           extra=[ordered]@{ 'Formato'='ATX'; 'Memoria'='DDR5, 4 ranuras'; 'Ranuras M2'='2'; 'WiFi'='WiFi 6E'; 'Chipset'='B650' } }
        @{ viejo='MB-AM5-ITX'; sku='MB-B650E-I-STRIX'; name='ASUS ROG STRIX B650E-I GAMING WIFI'; brand='ASUS'; cat='placas-base'; tipo='PLACA_BASE'
           desc='Placa base en formato Mini-ITX para socket AM5 con DDR5 y PCIe 5.0. Pensada para equipos compactos de alto rendimiento.'
           price=1450000; stock=3; min=1; socket='AM5'; ramSup=@('DDR5'); form='Mini-ITX'
           extra=[ordered]@{ 'Formato'='Mini-ITX'; 'Memoria'='DDR5, 2 ranuras'; 'Ranuras M2'='2'; 'PCIe'='5.0'; 'Chipset'='B650E' } }
        @{ viejo='MB-AM4-DDR4'; sku='MB-B550M-PRO-VDH'; name='MSI B550M PRO-VDH WIFI'; brand='MSI'; cat='placas-base'; tipo='PLACA_BASE'
           desc='Placa base microATX para socket AM4 con memoria DDR4 y red inalambrica. La base mas economica para armar un equipo AM4.'
           price=519000; stock=5; min=2; socket='AM4'; ramSup=@('DDR4'); form='microATX'
           extra=[ordered]@{ 'Formato'='microATX'; 'Memoria'='DDR4, 4 ranuras'; 'Ranuras M2'='1'; 'WiFi'='Si'; 'Chipset'='B550' } }
        @{ viejo='MB-LGA1700-DDR4'; sku='MB-B760M-DS3H'; name='Gigabyte B760M DS3H DDR4'; brand='Gigabyte'; cat='placas-base'; tipo='PLACA_BASE'
           desc='Placa base microATX para socket LGA1700 con memoria DDR4. Permite reutilizar modulos DDR4 en una plataforma reciente.'
           price=629000; stock=6; min=2; socket='LGA1700'; ramSup=@('DDR4'); form='microATX'
           extra=[ordered]@{ 'Formato'='microATX'; 'Memoria'='DDR4, 4 ranuras'; 'Ranuras M2'='2'; 'Chipset'='B760' } }
        @{ viejo=$null; sku='MB-Z790-P-PRIME'; name='ASUS PRIME Z790-P WIFI'; brand='ASUS'; cat='placas-base'; tipo='PLACA_BASE'
           desc='Placa base ATX para socket LGA1700 con memoria DDR5, chipset Z790 y PCIe 5.0.'
           price=1390000; stock=5; min=1; socket='LGA1700'; ramSup=@('DDR5'); form='ATX'
           extra=[ordered]@{ 'Formato'='ATX'; 'Memoria'='DDR5, 4 ranuras'; 'Ranuras M2'='4'; 'PCIe'='5.0'; 'Chipset'='Z790' } }
        @{ viejo='MB-LGA1851-DDR5'; sku='MB-Z890-PRO-RS'; name='ASRock Z890 Pro RS'; brand='ASRock'; cat='placas-base'; tipo='PLACA_BASE'
           desc='Placa base ATX para el socket LGA1851 con memoria DDR5 y PCIe 5.0. La plataforma mas reciente de Intel en el catalogo.'
           price=1250000; stock=4; min=1; socket='LGA1851'; ramSup=@('DDR5'); form='ATX'
           extra=[ordered]@{ 'Formato'='ATX'; 'Memoria'='DDR5, 4 ranuras'; 'Ranuras M2'='4'; 'PCIe'='5.0'; 'Chipset'='Z890' } }
        @{ viejo='MB-AM5-SIN-RAM'; sku='MB-OEM-SIN-RAM'; name='Placa base OEM sin tipos de memoria publicados'; brand='Generica'; cat='placas-base'; tipo='PLACA_BASE'
           desc='Producto de prueba que declara el socket pero no los tipos de memoria que admite, para verificar el aviso de datos faltantes.'
           price=610000; stock=2; min=1; socket='AM5'; ramSup=$null; form='ATX'
           extra=[ordered]@{ 'Nota'='Sin tipos de memoria declarados' } }
    )

    $memorias = @(
        @{ viejo='RAM-DDR4-8'; sku='RAM-DDR4-8-3200'; name='Kingston Fury Beast DDR4 8 GB 3200 MHz'; brand='Kingston'; cat='memorias-ram'; tipo='MEMORIA_RAM'
           desc='Modulo de memoria DDR4 de 8 GB a 3200 MHz con disipador de perfil bajo.'
           price=189000; stock=12; min=4; ramType='DDR4'; powerW=8
           extra=[ordered]@{ 'Capacidad'='8 GB'; 'Tipo'='DDR4'; 'Velocidad'='3200 MHz'; 'Latencia'='CL16' } }
        @{ viejo='RAM-DDR4-16'; sku='RAM-DDR4-16-3200'; name='Crucial Pro DDR4 16 GB 3200 MHz'; brand='Crucial'; cat='memorias-ram'; tipo='MEMORIA_RAM'
           desc='Modulo de memoria DDR4 de 16 GB a 3200 MHz. La opcion mas recomendable para placas que solo admiten DDR4.'
           price=319000; stock=9; min=3; ramType='DDR4'; powerW=9
           extra=[ordered]@{ 'Capacidad'='16 GB'; 'Tipo'='DDR4'; 'Velocidad'='3200 MHz'; 'Latencia'='CL22' } }
        @{ viejo='RAM-DDR5-16'; sku='RAM-DDR5-16-5600'; name='Kingston Fury Beast DDR5 16 GB 5600 MHz'; brand='Kingston'; cat='memorias-ram'; tipo='MEMORIA_RAM'
           desc='Modulo de memoria DDR5 de 16 GB a 5600 MHz con perfil EXPO para plataformas AMD.'
           price=349000; stock=10; min=3; ramType='DDR5'; powerW=10
           extra=[ordered]@{ 'Capacidad'='16 GB'; 'Tipo'='DDR5'; 'Velocidad'='5600 MHz'; 'Perfil'='EXPO y XMP' } }
        @{ viejo='RAM-DDR5-32'; sku='RAM-DDR5-32-6000'; name='Corsair Vengeance DDR5 32 GB 6000 MHz'; brand='Corsair'; cat='memorias-ram'; tipo='MEMORIA_RAM'
           desc='Modulo de memoria DDR5 de 32 GB a 6000 MHz. Recomendado para equipos de juego exigentes y edicion.'
           price=689000; stock=6; min=2; ramType='DDR5'; powerW=12
           extra=[ordered]@{ 'Capacidad'='32 GB'; 'Tipo'='DDR5'; 'Velocidad'='6000 MHz'; 'Latencia'='CL30'; 'Perfil'='EXPO y XMP' } }
        @{ viejo='RAM-DDR3-8'; sku='RAM-DDR3-8-OEM'; name='Memoria DDR3 8 GB sin marca reconocida'; brand='Generica'; cat='memorias-ram'; tipo='MEMORIA_RAM'
           desc='Modulo de memoria DDR3 de 8 GB. Se mantiene en el catalogo porque es incompatible con todas las placas vigentes y sirve para demostrarlo.'
           price=95000; stock=6; min=2; ramType='DDR3'; powerW=7
           extra=[ordered]@{ 'Capacidad'='8 GB'; 'Tipo'='DDR3'; 'Velocidad'='1600 MHz' } }
    )

    $fuentes = @(
        @{ viejo='PSU-450W-BAS'; sku='PSU-450W-OEM'; name='Fuente OEM 450 W sin certificar'; brand='Generica'; cat='fuentes-de-poder'; tipo='FUENTE_PODER'
           desc='Fuente de 450 W sin certificacion. Suficiente para equipos basicos e insuficiente para procesadores y graficas de gama alta.'
           price=185000; stock=9; min=3; capW=450
           extra=[ordered]@{ 'Potencia'='450 W'; 'Certificacion'='Sin certificar'; 'Modular'='No' } }
        @{ viejo='PSU-650W-80P'; sku='PSU-650W-BRONZE'; name='EVGA 650 BR 650 W 80 PLUS Bronze'; brand='EVGA'; cat='fuentes-de-poder'; tipo='FUENTE_PODER'
           desc='Fuente de 650 W con certificacion 80 PLUS Bronze. El punto de partida razonable para un equipo de gama media.'
           price=425000; stock=7; min=2; capW=650
           extra=[ordered]@{ 'Potencia'='650 W'; 'Certificacion'='80 PLUS Bronze'; 'Modular'='No' } }
        @{ viejo='PSU-850W-80G'; sku='PSU-850W-GOLD'; name='Corsair RM850 850 W 80 PLUS Gold'; brand='Corsair'; cat='fuentes-de-poder'; tipo='FUENTE_PODER'
           desc='Fuente de 850 W con certificacion 80 PLUS Gold y cableado totalmente modular.'
           price=729000; stock=5; min=1; capW=850
           extra=[ordered]@{ 'Potencia'='850 W'; 'Certificacion'='80 PLUS Gold'; 'Modular'='Totalmente modular' } }
        @{ viejo='PSU-1000W-80P'; sku='PSU-1000W-PLATINUM'; name='Seasonic PRIME PX-1000 1000 W 80 PLUS Platinum'; brand='Seasonic'; cat='fuentes-de-poder'; tipo='FUENTE_PODER'
           desc='Fuente de 1000 W con certificacion 80 PLUS Platinum. Necesaria para combinar un procesador de gama alta con una grafica de alto consumo.'
           price=1250000; stock=3; min=1; capW=1000
           extra=[ordered]@{ 'Potencia'='1000 W'; 'Certificacion'='80 PLUS Platinum'; 'Modular'='Totalmente modular' } }
    )

    $graficas = @(
        @{ viejo='GPU-RTX-4060'; sku='GPU-RTX4060-8G'; name='ASUS Dual GeForce RTX 4060 OC 8 GB'; brand='ASUS'; cat='tarjetas-graficas'; tipo='TARJETA_GRAFICA'
           desc='Tarjeta grafica de gama media con 8 GB de memoria. Consumo contenido, adecuada para fuentes de 450 W en adelante.'
           price=1580000; stock=4; min=1; powerW=115
           extra=[ordered]@{ 'Memoria'='8 GB GDDR6'; 'Consumo'='115 W'; 'Alimentacion'='1 conector de 8 pines'; 'Salidas'='3x DisplayPort, 1x HDMI' } }
        @{ viejo='GPU-RTX-4070'; sku='GPU-RTX4070-12G'; name='Gigabyte GeForce RTX 4070 WINDFORCE OC 12 GB'; brand='Gigabyte'; cat='tarjetas-graficas'; tipo='TARJETA_GRAFICA'
           desc='Tarjeta grafica de gama alta con 12 GB de memoria, orientada a juego en resoluciones altas y edicion.'
           price=2650000; stock=3; min=1; powerW=200
           extra=[ordered]@{ 'Memoria'='12 GB GDDR6X'; 'Consumo'='200 W'; 'Alimentacion'='1 conector de 8 pines'; 'Salidas'='3x DisplayPort, 1x HDMI' } }
        @{ viejo=$null; sku='GPU-RTX4080S-16G'; name='ASUS TUF Gaming GeForce RTX 4080 SUPER 16 GB'; brand='ASUS'; cat='tarjetas-graficas'; tipo='TARJETA_GRAFICA'
           desc='Tarjeta grafica de gama muy alta con 16 GB de memoria. Requiere fuente de 750 W o superior.'
           price=5450000; stock=2; min=1; powerW=320
           extra=[ordered]@{ 'Memoria'='16 GB GDDR6X'; 'Consumo'='320 W'; 'Alimentacion'='1 conector de 16 pines'; 'Salidas'='3x DisplayPort, 2x HDMI' } }
        @{ viejo='GPU-RTX-4090'; sku='GPU-RTX4090-24G'; name='ASUS TUF Gaming GeForce RTX 4090 24 GB'; brand='ASUS'; cat='tarjetas-graficas'; tipo='TARJETA_GRAFICA'
           desc='Tarjeta grafica de gama entusiasta con 24 GB de memoria y 450 W de consumo. Exige fuente de alta capacidad y un gabinete amplio.'
           price=8900000; stock=1; min=1; powerW=450
           extra=[ordered]@{ 'Memoria'='24 GB GDDR6X'; 'Consumo'='450 W'; 'Alimentacion'='1 conector de 16 pines'; 'Salidas'='3x DisplayPort, 2x HDMI' } }
        @{ viejo='GPU-OEM-IND'; sku='GPU-OEM-SIN-CONSUMO'; name='Tarjeta grafica OEM sin consumo publicado'; brand='Generica'; cat='tarjetas-graficas'; tipo='TARJETA_GRAFICA'
           desc='Producto de prueba que no declara su consumo, para verificar que el validador avisa del dato faltante en lugar de estimar la potencia.'
           price=890000; stock=2; min=1; powerW=$null
           extra=[ordered]@{ 'Memoria'='8 GB'; 'Nota'='Sin consumo declarado por el fabricante' } }
    )

    $almacenamiento = @(
        @{ viejo='SSD-NVME-1TB'; sku='SSD-990PRO-1TB'; name='Samsung 990 PRO 1 TB NVMe PCIe 4.0'; brand='Samsung'; cat='almacenamiento'; tipo='ALMACENAMIENTO'
           desc='Unidad de estado solido NVMe de 1 TB sobre PCIe 4.0, con velocidades de lectura de hasta 7450 MB/s.'
           price=549000; stock=10; min=3; powerW=8
           extra=[ordered]@{ 'Capacidad'='1 TB'; 'Interfaz'='NVMe PCIe 4.0'; 'Lectura secuencial'='7450 MB/s'; 'Escritura secuencial'='6900 MB/s'; 'Formato'='M.2 2280' } }
        @{ viejo='SSD-SATA-480'; sku='SSD-A400-480G'; name='Kingston A400 480 GB SATA III'; brand='Kingston'; cat='almacenamiento'; tipo='ALMACENAMIENTO'
           desc='Unidad de estado solido SATA III de 480 GB. La opcion economica para arrancar el sistema.'
           price=215000; stock=12; min=4; powerW=5
           extra=[ordered]@{ 'Capacidad'='480 GB'; 'Interfaz'='SATA III'; 'Lectura secuencial'='500 MB/s'; 'Formato'='2.5 pulgadas' } }
        @{ viejo='HDD-SATA-2TB'; sku='HDD-BARRACUDA-2TB'; name='Seagate Barracuda 2 TB 7200 rpm SATA III'; brand='Seagate'; cat='almacenamiento'; tipo='ALMACENAMIENTO'
           desc='Disco duro mecanico de 2 TB a 7200 rpm para almacenamiento masivo.'
           price=329000; stock=8; min=2; powerW=9
           extra=[ordered]@{ 'Capacidad'='2 TB'; 'Interfaz'='SATA III'; 'Velocidad'='7200 rpm'; 'Cache'='256 MB'; 'Formato'='3.5 pulgadas' } }
    )

    $gabinetes = @(
        @{ viejo='CASE-ATX-MID'; sku='CASE-H5-FLOW'; name='NZXT H5 Flow'; brand='NZXT'; cat='gabinetes'; tipo='GABINETE'
           desc='Gabinete de torre media para placas ATX y microATX, con frontal de malla y dos ventiladores incluidos.'
           price=459000; stock=6; min=2; form='ATX'
           extra=[ordered]@{ 'Formato admitido'='ATX y microATX'; 'Ventiladores incluidos'='2'; 'Bahias de discos de 3,5 pulgadas'='1'; 'Bahias de discos de 2,5 pulgadas'='2'; 'Largo maximo de grafica'='365 mm' } }
        @{ viejo=$null; sku='CASE-Q300L'; name='Cooler Master MasterBox Q300L'; brand='Cooler Master'; cat='gabinetes'; tipo='GABINETE'
           desc='Gabinete compacto para placas microATX y Mini-ITX, con panel lateral perforado y filtros antipolvo.'
           price=259000; stock=7; min=2; form='microATX'
           extra=[ordered]@{ 'Formato admitido'='microATX y Mini-ITX'; 'Ventiladores incluidos'='1'; 'Bahias de discos de 3,5 pulgadas'='1'; 'Bahias de discos de 2,5 pulgadas'='2'; 'Largo maximo de grafica'='360 mm' } }
        @{ viejo='CASE-ITX-SLIM'; sku='CASE-NR200P'; name='Cooler Master NR200P'; brand='Cooler Master'; cat='gabinetes'; tipo='GABINETE'
           desc='Gabinete Mini-ITX de 18 litros con panel lateral de cristal templado. Espacio reducido: limita el largo de la grafica y la altura del disipador.'
           price=429000; stock=5; min=2; form='Mini-ITX'
           extra=[ordered]@{ 'Formato admitido'='Mini-ITX'; 'Ventiladores incluidos'='2'; 'Bahias de discos de 3,5 pulgadas'='1'; 'Bahias de discos de 2,5 pulgadas'='2'; 'Largo maximo de grafica'='330 mm' } }
    )

    $refrigeracion = @(
        @{ viejo='COOL-AIR-150'; sku='COOL-HYPER212'; name='Cooler Master Hyper 212 Black Edition'; brand='Cooler Master'; cat='refrigeracion'; tipo='REFRIGERACION'
           desc='Disipador por aire de torre con ventilador de 120 mm, para procesadores de hasta 150 W de TDP.'
           price=219000; stock=9; min=3; powerW=4
           extra=[ordered]@{ 'Tipo'='Aire'; 'TDP soportado'='150 W'; 'Ventilador'='120 mm'; 'Altura'='159 mm'; 'Zocalos'='AM4, AM5, LGA1700, LGA1851' } }
        @{ viejo=$null; sku='COOL-NHD15'; name='Noctua NH-D15'; brand='Noctua'; cat='refrigeracion'; tipo='REFRIGERACION'
           desc='Disipador por aire de doble torre con dos ventiladores, para procesadores de hasta 220 W de TDP. Muy alto: comprobar el ancho del gabinete.'
           price=749000; stock=4; min=1; powerW=5
           extra=[ordered]@{ 'Tipo'='Aire'; 'TDP soportado'='220 W'; 'Ventilador'='2 de 140 mm'; 'Altura'='165 mm'; 'Zocalos'='AM4, AM5, LGA1700, LGA1851' } }
        @{ viejo='COOL-LIQ-240'; sku='COOL-H100I-240'; name='Corsair iCUE H100i ELITE CAPELLIX XT 240 mm'; brand='Corsair'; cat='refrigeracion'; tipo='REFRIGERACION'
           desc='Refrigeracion liquida con radiador de 240 mm y dos ventiladores, para procesadores de hasta 250 W de TDP.'
           price=829000; stock=4; min=1; powerW=12
           extra=[ordered]@{ 'Tipo'='Liquida'; 'TDP soportado'='250 W'; 'Radiador'='240 mm'; 'Ventilador'='2 de 120 mm'; 'Zocalos'='AM4, AM5, LGA1700, LGA1851' } }
        @{ viejo=$null; sku='COOL-LF3-360'; name='ARCTIC Liquid Freezer III 360'; brand='ARCTIC'; cat='refrigeracion'; tipo='REFRIGERACION'
           desc='Refrigeracion liquida con radiador de 360 mm y tres ventiladores, para procesadores de hasta 350 W de TDP. Necesaria para un procesador de gama entusiasta.'
           price=1090000; stock=3; min=1; powerW=15
           extra=[ordered]@{ 'Tipo'='Liquida'; 'TDP soportado'='350 W'; 'Radiador'='360 mm'; 'Ventilador'='3 de 120 mm'; 'Zocalos'='AM4, AM5, LGA1700, LGA1851' } }
    )

    $accesorios = @(
        @{ viejo='ACC-CABLES-KIT'; sku='ACC-CABLES-MOD'; name='Kit de cables de alimentacion modular'; brand='Generica'; cat='accesorios'; tipo='OTRO'
           desc='Juego de cables de alimentacion modular. Se deja a proposito sin ningun atributo tecnico: comprueba que el configurador y el validador aceptan un componente que no participa en ninguna regla de compatibilidad.'
           price=89000; stock=15; min=5 }
    )

    return @($procesadores + $placas + $memorias + $fuentes + $graficas + $almacenamiento + $gabinetes + $refrigeracion + $accesorios)
}
