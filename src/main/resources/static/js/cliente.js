
document.addEventListener("DOMContentLoaded", function () {


    function dinero(valor) {
        return "$" + valor.toLocaleString("en-US", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    }


    function minutos(texto) {
        if (!texto) {
            return null;
        }
        var partes = texto.split(":");
        return parseInt(partes[0], 10) * 60 + parseInt(partes[1], 10);
    }


    function hoyISO() {
        var ahora = new Date();
        var mes = String(ahora.getMonth() + 1).padStart(2, "0");
        var dia = String(ahora.getDate()).padStart(2, "0");
        return ahora.getFullYear() + "-" + mes + "-" + dia;
    }


    // --- Fecha no pasada y, si es hoy, hora no pasada ---------------------
    document.querySelectorAll("form[data-validar-fecha]").forEach(function (formulario) {


        var fecha = formulario.querySelector("[name='fecha']");
        var hora = formulario.querySelector("[name='" + formulario.dataset.campoHora + "']");


        if (!fecha || !hora) {
            return;
        }


        fecha.min = hoyISO();


        function validar() {
            hora.setCustomValidity("");


            if (fecha.value === hoyISO() && hora.value) {
                var ahora = new Date();
                var actual = ahora.getHours() * 60 + ahora.getMinutes();


                if (minutos(hora.value) <= actual) {
                    hora.setCustomValidity("Esa hora ya pasó; elige una hora posterior a la actual");
                }
            }
        }


        fecha.addEventListener("input", validar);
        hora.addEventListener("input", validar);
        validar();
    });


    // --- Mis datos: formato de DUI/NIT y teléfono --------------------------
    // El patrón y el mensaje vienen del servidor (FormatoContacto), que
    // vuelve a validar todo.
    function formatearDui(valor) {
        var digitos = valor.replace(/\D/g, "").slice(0, 14);


        if (digitos.length <= 9) {
            return digitos.length > 8
                ? digitos.slice(0, 8) + "-" + digitos.slice(8)
                : digitos;
        }


        return [digitos.slice(0, 4), digitos.slice(4, 10), digitos.slice(10, 13), digitos.slice(13)]
            .filter(Boolean)
            .join("-");
    }


    function formatearTelefono(valor) {
        var conPrefijo = /^\s*\+/.test(valor);
        var digitos = valor.replace(/\D/g, "");


        // Mientras escribe "+5", "+50" o "+503" se deja tal cual.
        if (conPrefijo && "503".indexOf(digitos) === 0) {
            return valor.trim();
        }


        if (conPrefijo) {
            digitos = digitos.replace(/^503/, "");
        }


        digitos = digitos.slice(0, 8);
        var numero = digitos.length > 4
            ? digitos.slice(0, 4) + "-" + digitos.slice(4)
            : digitos;


        return conPrefijo ? "+503 " + numero : numero;
    }


    document.querySelectorAll("input[data-formato]").forEach(function (campo) {
        var formatear = campo.dataset.formato === "dui" ? formatearDui : formatearTelefono;
        var patron = new RegExp("^(?:" + campo.getAttribute("pattern") + ")$");
        var errorServidor = campo.parentElement.querySelector(".texto-error");
        var error = document.createElement("span");


        error.className = "texto-error";
        error.hidden = true;
        campo.parentElement.appendChild(error);


        function validar() {
            var valido = campo.value === "" || patron.test(campo.value);


            campo.setCustomValidity(valido ? "" : campo.dataset.mensaje);
            campo.classList.toggle("invalido", !valido);
            error.textContent = valido ? "" : campo.dataset.mensaje;
            error.hidden = valido;
        }


        campo.addEventListener("input", function () {
            // Solo se reformatea con el cursor al final para no moverlo al corregir en medio.
            if (campo.selectionStart === campo.value.length) {
                campo.value = formatear(campo.value);
            }
            if (errorServidor) {
                errorServidor.hidden = true;
            }
            if (!error.hidden) {
                validar();
            }
        });


        campo.addEventListener("blur", function () {
            campo.value = formatear(campo.value);
            validar();
        });


        campo.addEventListener("invalid", function () {
            if (campo.value !== "") {
                validar();
            }
        });
    });


    // --- Alquiler: costo por hora × horas ---------------------------------
    var formularioAlquiler = document.querySelector("form[data-costo-hora]");


    if (formularioAlquiler) {
        var costoHora = parseFloat(formularioAlquiler.dataset.costoHora) || 0;
        var inicio = formularioAlquiler.querySelector("[name='horaInicio']");
        var fin = formularioAlquiler.querySelector("[name='horaFin']");
        var salida = document.getElementById("costoEstimado");
        var detalle = document.getElementById("horasEstimadas");


        var calcularAlquiler = function () {
            var desde = minutos(inicio.value);
            var hasta = minutos(fin.value);


            fin.setCustomValidity("");


            if (desde === null || hasta === null) {
                salida.textContent = "$0.00";
                detalle.textContent = "Elige el horario";
                return;
            }


            if (hasta <= desde) {
                fin.setCustomValidity("La hora de finalización debe ser posterior a la hora de inicio");
                salida.textContent = "$0.00";
                detalle.textContent = "Horario no válido";
                return;
            }


            var horas = (hasta - desde) / 60;
            salida.textContent = dinero(horas * costoHora);
            detalle.textContent = horas.toLocaleString("es", { maximumFractionDigits: 2 })
                + (horas === 1 ? " hora" : " horas");
        };


        inicio.addEventListener("input", calcularAlquiler);
        fin.addEventListener("input", calcularAlquiler);
        calcularAlquiler();
    }


    // --- Catering: precio por persona × asistentes ------------------------
    var formularioCatering = document.querySelector("form[data-precio-persona]");


    if (formularioCatering) {
        var precio = parseFloat(formularioCatering.dataset.precioPersona) || 0;
        var asistentes = formularioCatering.querySelector("[name='cantidadAsistentes']");
        var salidaCatering = document.getElementById("costoEstimado");


        var calcularCatering = function () {
            var cantidad = parseInt(asistentes.value, 10) || 0;
            salidaCatering.textContent = dinero(cantidad * precio);
        };


        asistentes.addEventListener("input", calcularCatering);
        calcularCatering();
    }


    // --- Cotización: líneas dinámicas --------------------------------------
    var cuerpo = document.getElementById("cuerpoLineas");
    var plantilla = document.getElementById("plantillaLinea");


    if (!cuerpo || !plantilla) {
        return;
    }


    function renumerar() {
        cuerpo.querySelectorAll("tr").forEach(function (fila, indice) {
            fila.querySelectorAll("[data-campo]").forEach(function (campo) {
                campo.name = "lineas[" + indice + "]." + campo.dataset.campo;
            });
        });
    }


    function actualizarFila(fila) {
        var tipo = fila.querySelector("[data-campo='tipo']").value;
        var item = fila.querySelector("[data-campo='idItem']");
        var descripcion = fila.querySelector("[data-campo='descripcion']");
        var unidad = fila.querySelector(".unidad");
        var esOtro = tipo === "OTRO";


        // Solo se muestran las opciones del tipo elegido.
        item.querySelectorAll("option[data-tipo]").forEach(function (opcion) {
            var visible = opcion.dataset.tipo === tipo;
            opcion.hidden = !visible;
            opcion.disabled = !visible;
        });


        var elegida = item.selectedOptions[0];
        if (elegida && elegida.dataset.tipo && elegida.dataset.tipo !== tipo) {
            item.value = "";
        }


        item.hidden = esOtro || !tipo;
        item.required = !esOtro && !!tipo;
        descripcion.hidden = !esOtro;
        descripcion.required = esOtro;


        var unidades = {
            CURSO: "participantes",
            DIPLOMADO: "participantes",
            ESPACIO: "horas",
            CATERING: "personas",
            OTRO: "unidades"
        };
        unidad.textContent = unidades[tipo] || "";


        calcularTotales();
    }


    function calcularTotales() {
        var subtotal = 0;


        cuerpo.querySelectorAll("tr").forEach(function (fila) {
            var tipo = fila.querySelector("[data-campo='tipo']").value;
            var item = fila.querySelector("[data-campo='idItem']");
            var cantidad = parseInt(fila.querySelector("[data-campo='cantidad']").value, 10) || 0;
            var opcion = item.selectedOptions[0];
            var precio = tipo !== "OTRO" && opcion && opcion.dataset.precio
                ? parseFloat(opcion.dataset.precio)
                : 0;
            var linea = precio * cantidad;


            fila.querySelector(".subtotal-linea").textContent =
                tipo === "OTRO" ? "Por cotizar" : dinero(linea);
            subtotal += linea;
        });


        document.getElementById("subtotalEstimado").textContent = dinero(subtotal);
        document.getElementById("totalEstimado").textContent = dinero(subtotal * 1.13);
    }


    function prepararFila(fila) {
        fila.querySelector("[data-campo='tipo']").addEventListener("change", function () {
            actualizarFila(fila);
        });
        fila.querySelector("[data-campo='idItem']").addEventListener("change", calcularTotales);
        fila.querySelector("[data-campo='cantidad']").addEventListener("input", calcularTotales);
        fila.querySelector(".boton-quitar").addEventListener("click", function () {
            if (cuerpo.querySelectorAll("tr").length <= 1) {
                alert("La solicitud debe tener al menos una línea.");
                return;
            }
            fila.remove();
            renumerar();
            calcularTotales();
        });
        actualizarFila(fila);
    }


    cuerpo.querySelectorAll("tr").forEach(prepararFila);


    document.getElementById("agregarLinea").addEventListener("click", function () {
        var fila = plantilla.content.firstElementChild.cloneNode(true);
        cuerpo.appendChild(fila);
        renumerar();
        prepararFila(fila);
    });


    renumerar();
    calcularTotales();
});


