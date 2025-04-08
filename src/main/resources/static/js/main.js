// main.js - Funzionalità JavaScript generali per il sito web

document.addEventListener("DOMContentLoaded", function () {
  // Inizializzazione funzionalità globali
  initGlobalFeatures();

  // Gestione dei toast/notifiche
  initNotificationSystem();

  // Gestione dei form
  initFormValidation();

  // Funzionalità di ricerca
  initSearchFunctionality();
});

/**
 * Inizializza funzionalità globali del sito
 */
function initGlobalFeatures() {
  // Tooltip Bootstrap
  initBootstrapTooltips();

  // Gestione del tema scuro/chiaro
  initThemeToggle();

  // Smooth scrolling per link interni
  initSmoothScrolling();
}

/**
 * Inizializza i tooltip di Bootstrap
 */
function initBootstrapTooltips() {
  const tooltipTriggerList = document.querySelectorAll(
    '[data-bs-toggle="tooltip"]'
  );
  const tooltipList = [...tooltipTriggerList].map(
    (tooltipTriggerEl) => new bootstrap.Tooltip(tooltipTriggerEl)
  );
}

/**
 * Implementa il toggle tra tema chiaro e scuro
 */
function initThemeToggle() {
  const themeToggle = document.getElementById("theme-toggle");

  if (themeToggle) {
    themeToggle.addEventListener("click", function () {
      document.body.classList.toggle("dark-theme");

      // Salva la preferenza del tema
      const theme = document.body.classList.contains("dark-theme")
        ? "dark"
        : "light";
      localStorage.setItem("site-theme", theme);
    });

    // Ripristina il tema precedente
    const savedTheme = localStorage.getItem("site-theme");
    if (savedTheme === "dark") {
      document.body.classList.add("dark-theme");
    }
  }
}

/**
 * Implementa smooth scrolling per link interni
 */
function initSmoothScrolling() {
  document.querySelectorAll('a[href^="#"]').forEach((anchor) => {
    anchor.addEventListener("click", function (e) {
      e.preventDefault();

      const targetId = this.getAttribute("href");
      const targetElement = document.querySelector(targetId);

      if (targetElement) {
        targetElement.scrollIntoView({
          behavior: "smooth",
        });
      }
    });
  });
}

/**
 * Sistema di notifiche
 */
function initNotificationSystem() {
  // Esempio di wrapper per mostrare notifiche
  window.showNotification = function (message, type = "info") {
    const notificationContainer = document.getElementById(
      "notification-container"
    );

    if (!notificationContainer) {
      const container = document.createElement("div");
      container.id = "notification-container";
      container.className = "notification-container";
      document.body.appendChild(container);
    }

    const notification = document.createElement("div");
    notification.className = `notification notification-${type}`;
    notification.textContent = message;

    notificationContainer.appendChild(notification);

    // Rimuovi la notifica dopo 3 secondi
    setTimeout(() => {
      notification.classList.add("fade-out");
      setTimeout(() => {
        notification.remove();
      }, 300);
    }, 3000);
  };
}

/**
 * Validazione form globale
 */
function initFormValidation() {
  const forms = document.querySelectorAll("form.needs-validation");

  forms.forEach((form) => {
    form.addEventListener(
      "submit",
      function (event) {
        if (!form.checkValidity()) {
          event.preventDefault();
          event.stopPropagation();
        }

        form.classList.add("was-validated");
      },
      false
    );

    // Reset validation stato quando l'utente modifica i campi
    const inputs = form.querySelectorAll("input, select, textarea");
    inputs.forEach((input) => {
      input.addEventListener("input", function () {
        if (input.validity.valid) {
          input.classList.remove("is-invalid");
          input.classList.add("is-valid");
        } else {
          input.classList.remove("is-valid");
          input.classList.add("is-invalid");
        }
      });
    });
  });
}

/**
 * Funzionalità di ricerca
 */
function initSearchFunctionality() {
  const searchInput = document.getElementById("search-input");
  const searchResults = document.getElementById("search-results");

  if (searchInput && searchResults) {
    searchInput.addEventListener("input", function () {
      const query = this.value.trim().toLowerCase();

      if (query.length < 2) {
        searchResults.innerHTML = "";
        return;
      }

      // Esempio di ricerca (da implementare con la tua logica specifica)
      performSearch(query);
    });
  }
}

/**
 * Funzione di ricerca (da personalizzare)
 * @param {string} query - Termine di ricerca
 */
function performSearch(query) {
  // Implementazione fittizia - sostituire con chiamata AJAX reale
  const mockResults = [
    { title: "Prodotto 1", url: "/products/1" },
    { title: "Prodotto 2", url: "/products/2" },
  ].filter((item) => item.title.toLowerCase().includes(query));

  const searchResults = document.getElementById("search-results");
  searchResults.innerHTML = mockResults
    .map(
      (result) =>
        `<a href="${result.url}" class="search-result-item">${result.title}</a>`
    )
    .join("");
}

// Aggiungi stili CSS per le nuove funzionalità
const styles = `
<style>
    /* Stili per il sistema di notifiche */
    .notification-container {
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 1050;
    }
    
    .notification {
        margin-bottom: 10px;
        padding: 15px;
        border-radius: 4px;
        color: white;
        opacity: 1;
        transition: opacity 0.3s ease;
    }
    
    .notification-info { background-color: #17a2b8; }
    .notification-success { background-color: #28a745; }
    .notification-warning { background-color: #ffc107; color: #212529; }
    .notification-error { background-color: #dc3545; }
    
    .notification.fade-out {
        opacity: 0;
    }
    
    /* Stili per temi chiaro/scuro */
    body.dark-theme {
        background-color: #121212;
        color: #ffffff;
    }
    
    body.dark-theme .card {
        background-color: #1e1e1e;
        color: #ffffff;
    }
    
    /* Stili per ricerca */
    #search-results {
        position: absolute;
        width: 100%;
        max-height: 300px;
        overflow-y: auto;
        background-color: white;
        border: 1px solid #ddd;
        box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        z-index: 1000;
    }
    
    .search-result-item {
        display: block;
        padding: 10px;
        text-decoration: none;
        color: #333;
    }
    
    .search-result-item:hover {
        background-color: #f0f0f0;
    }
</style>
`;

// Aggiungi gli stili al documento
document.head.insertAdjacentHTML("beforeend", styles);
