document.addEventListener("DOMContentLoaded", function () {


    document.querySelectorAll(".auth-ojo").forEach(function (boton) {
        boton.addEventListener("click", function () {
            var campo = document.getElementById(boton.dataset.objetivo);
            var mostrar = campo.type === "password";


            campo.type = mostrar ? "text" : "password";
            boton.setAttribute("aria-pressed", String(mostrar));
            boton.setAttribute(
                "aria-label",
                mostrar ? "Ocultar contraseña" : "Mostrar contraseña"
            );
        });
    });


    var password = document.getElementById("password");
    var confirmar = document.getElementById("confirmarPassword");


    if (password && confirmar) {
        var validarCoincidencia = function () {
            confirmar.setCustomValidity(
                confirmar.value && confirmar.value !== password.value
                    ? "Las contraseñas no coinciden"
                    : ""
            );
        };


        password.addEventListener("input", validarCoincidencia);
        confirmar.addEventListener("input", validarCoincidencia);
    }
});


