// cart.js - Funzioni JavaScript per interagire con le API del carrello

document.addEventListener("DOMContentLoaded", function () {
  // Inizializza gli eventi per i pulsanti di aggiunta al carrello
  initAddToCartButtons();

  // Inizializza gli eventi per modificare le quantità nel carrello
  initQuantityButtons();

  // Inizializza gli eventi per rimuovere gli elementi dal carrello
  initRemoveButtons();
});

/**
 * Inizializza i pulsanti per aggiungere prodotti al carrello
 */
function initAddToCartButtons() {
  const addToCartButtons = document.querySelectorAll(".add-to-cart-btn");

  addToCartButtons.forEach((button) => {
    button.addEventListener("click", function (event) {
      event.preventDefault();

      const productId = this.getAttribute("data-product-id");
      let quantity = 1;

      // Se esiste un campo di input per la quantità, usare quel valore
      const quantityInput =
        this.closest(".product-actions")?.querySelector(".quantity-input");
      if (quantityInput) {
        quantity = parseInt(quantityInput.value) || 1;
      }

      addToCart(productId, quantity);
    });
  });
}

/**
 * Inizializza i pulsanti per modificare le quantità nel carrello
 */
function initQuantityButtons() {
  const decreaseButtons = document.querySelectorAll(".quantity-decrease");
  const increaseButtons = document.querySelectorAll(".quantity-increase");

  decreaseButtons.forEach((button) => {
    button.addEventListener("click", function () {
      const cartItemId = this.getAttribute("data-item-id");
      const quantityElement = document.getElementById(
        `item-quantity-${cartItemId}`
      );
      let currentQuantity = parseInt(quantityElement.textContent);

      if (currentQuantity > 1) {
        updateQuantity(cartItemId, currentQuantity - 1);
      } else {
        // Se la quantità è 1 e si preme il pulsante di diminuzione,
        // chiedi conferma prima di rimuovere l'elemento
        if (confirm("Vuoi rimuovere questo prodotto dal carrello?")) {
          removeFromCart(cartItemId);
        }
      }
    });
  });

  increaseButtons.forEach((button) => {
    button.addEventListener("click", function () {
      const cartItemId = this.getAttribute("data-item-id");
      const quantityElement = document.getElementById(
        `item-quantity-${cartItemId}`
      );
      let currentQuantity = parseInt(quantityElement.textContent);

      updateQuantity(cartItemId, currentQuantity + 1);
    });
  });
}

/**
 * Inizializza i pulsanti per rimuovere elementi dal carrello
 */
function initRemoveButtons() {
  const removeButtons = document.querySelectorAll(".remove-item-btn");

  removeButtons.forEach((button) => {
    button.addEventListener("click", function () {
      const cartItemId = this.getAttribute("data-item-id");

      if (
        confirm("Sei sicuro di voler rimuovere questo prodotto dal carrello?")
      ) {
        removeFromCart(cartItemId);
      }
    });
  });
}

/**
 * Aggiunge un prodotto al carrello
 * @param {string} productId - ID del prodotto
 * @param {number} quantity - Quantità da aggiungere
 */
function addToCart(productId, quantity) {
  // Ottiene il token CSRF dalla pagina
  const csrfToken = document
    .querySelector('meta[name="_csrf"]')
    .getAttribute("content");
  const csrfHeader = document
    .querySelector('meta[name="_csrf_header"]')
    .getAttribute("content");

  // Prepara i dati per la richiesta
  const formData = new FormData();
  formData.append("quantity", quantity);

  // Effettua la richiesta POST all'API
  fetch(`/cart/add/${productId}`, {
    method: "POST",
    headers: {
      [csrfHeader]: csrfToken,
    },
    body: formData,
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.success) {
        // Aggiorna il contatore del carrello
        updateCartCounter(data.cartItemCount);

        // Mostra un messaggio di notifica
        showNotification(data.message, "success");
      } else {
        showNotification(data.message, "error");

        // Se l'utente non è autenticato, reindirizza al login
        if (data.message.includes("login")) {
          setTimeout(() => {
            window.location.href = "/login";
          }, 2000);
        }
      }
    })
    .catch((error) => {
      console.error("Errore durante l'aggiunta al carrello:", error);
      showNotification(
        "Si è verificato un errore durante l'aggiunta al carrello",
        "error"
      );
    });
}

/**
 * Aggiorna la quantità di un elemento nel carrello
 * @param {string} cartItemId - ID dell'elemento nel carrello
 * @param {number} quantity - Nuova quantità
 */
function updateQuantity(cartItemId, quantity) {
  // Ottiene il token CSRF dalla pagina
  const csrfToken = document
    .querySelector('meta[name="_csrf"]')
    .getAttribute("content");
  const csrfHeader = document
    .querySelector('meta[name="_csrf_header"]')
    .getAttribute("content");

  // Prepara i dati per la richiesta
  const formData = new FormData();
  formData.append("quantity", quantity);

  // Effettua la richiesta PUT all'API
  fetch(`/cart/update/${cartItemId}`, {
    method: "PUT",
    headers: {
      [csrfHeader]: csrfToken,
    },
    body: formData,
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.success) {
        // Aggiorna l'interfaccia utente
        const quantityElement = document.getElementById(
          `item-quantity-${cartItemId}`
        );
        const itemTotalElement = document.getElementById(
          `item-total-${cartItemId}`
        );
        const cartTotalElement = document.getElementById("cart-total");

        if (quantityElement) {
          quantityElement.textContent = quantity;
        }

        if (itemTotalElement) {
          itemTotalElement.textContent = formatCurrency(data.itemTotal);
        }

        if (cartTotalElement) {
          cartTotalElement.textContent = formatCurrency(data.cartTotal);
        }

        // Aggiorna il contatore del carrello
        updateCartCounter(data.cartItemCount);

        // Aggiorna anche il totale con spedizione
        updateCartTotalWithShipping(data.cartTotal);
      } else {
        showNotification(data.message, "error");
      }
    })
    .catch((error) => {
      console.error("Errore durante l'aggiornamento della quantità:", error);
      showNotification(
        "Si è verificato un errore durante l'aggiornamento della quantità",
        "error"
      );
    });
}

/**
 * Rimuove un elemento dal carrello
 * @param {string} cartItemId - ID dell'elemento nel carrello
 */
function removeFromCart(cartItemId) {
  // Ottiene il token CSRF dalla pagina
  const csrfToken = document
    .querySelector('meta[name="_csrf"]')
    .getAttribute("content");
  const csrfHeader = document
    .querySelector('meta[name="_csrf_header"]')
    .getAttribute("content");

  // Effettua la richiesta DELETE all'API
  fetch(`/cart/remove/${cartItemId}`, {
    method: "DELETE",
    headers: {
      [csrfHeader]: csrfToken,
    },
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.success) {
        // Rimuove l'elemento dalla pagina
        const cartItemElement = document.getElementById(
          `cart-item-${cartItemId}`
        );
        if (cartItemElement) {
          cartItemElement.remove();
        }

        // Aggiorna il totale del carrello
        const cartTotalElement = document.getElementById("cart-total");
        if (cartTotalElement) {
          cartTotalElement.textContent = formatCurrency(data.cartTotal);
        }

        // Aggiorna il contatore del carrello
        updateCartCounter(data.cartItemCount);

        // Aggiorna anche il totale con spedizione
        updateCartTotalWithShipping(data.cartTotal);

        // Mostra un messaggio di notifica
        showNotification(data.message, "success");

        // Se il carrello è vuoto, aggiorna l'interfaccia
        if (data.cartItemCount === 0) {
          showEmptyCartMessage();
        }
      } else {
        showNotification(data.message, "error");
      }
    })
    .catch((error) => {
      console.error("Errore durante la rimozione dal carrello:", error);
      showNotification(
        "Si è verificato un errore durante la rimozione dal carrello",
        "error"
      );
    });
}

/**
 * Mostra il messaggio di carrello vuoto
 */
function showEmptyCartMessage() {
  const cartItemsContainer = document.querySelector(".cart-items");
  const cartSummary = document.querySelector(".cart-summary");
  const cartActions = document.querySelector(".cart-actions");

  if (cartItemsContainer) {
    // Svuota il contenitore degli elementi
    cartItemsContainer.innerHTML = `
            <div class="empty-cart">
                <p>Il tuo carrello è vuoto</p>
            </div>
        `;
  }

  // Nascondi il riepilogo e i pulsanti di azione
  if (cartSummary) {
    cartSummary.style.display = "none";
  }

  // Modifica i pulsanti di azione per mostrare solo "Continua lo shopping"
  if (cartActions) {
    cartActions.innerHTML = `
            <a href="/products" class="btn btn-primary">Continua lo shopping</a>
        `;
  }
}

/**
 * Aggiorna il contatore del carrello nell'header
 * @param {number} count - Numero di elementi nel carrello
 */
function updateCartCounter(count) {
  const cartCounter = document.querySelector(".cart-counter");

  if (cartCounter) {
    cartCounter.textContent = count;

    // Nasconde il contatore se è 0
    if (count === 0) {
      cartCounter.style.display = "none";
    } else {
      cartCounter.style.display = "flex";
    }
  }
}

/**
 * Aggiorna il totale del carrello con le spese di spedizione
 * @param {number} total - Totale del carrello senza spedizione
 */
function updateCartTotalWithShipping(total) {
  const cartTotalWithShippingElement = document.querySelector(
    ".cart-total-amount span:last-child"
  );

  if (cartTotalWithShippingElement) {
    // Calcola il nuovo totale con la spedizione (gratuita sopra i 50€)
    const shippingCost = total > 50 ? 0 : 4.99;
    const totalWithShipping = total + shippingCost;

    cartTotalWithShippingElement.textContent =
      formatCurrency(totalWithShipping);
  }
}

/**
 * Formatta un valore come valuta
 * @param {number} value - Valore da formattare
 * @returns {string} Valore formattato come valuta
 */
function formatCurrency(value) {
  return new Intl.NumberFormat("it-IT", {
    style: "currency",
    currency: "EUR",
  }).format(value);
}

/**
 * Mostra una notifica all'utente
 * @param {string} message - Messaggio da mostrare
 * @param {string} type - Tipo di notifica (success, error, info)
 */
function showNotification(message, type = "info") {
  // Rimuove eventuali notifiche esistenti
  const existingNotifications = document.querySelectorAll(".notification");
  existingNotifications.forEach((notification) => {
    notification.remove();
  });

  // Crea l'elemento di notifica
  const notification = document.createElement("div");
  notification.className = `notification notification-${type}`;
  notification.textContent = message;

  // Aggiunge la notifica al DOM
  document.body.appendChild(notification);

  // Attende un attimo prima di mostrare la notifica (per l'animazione)
  setTimeout(() => {
    notification.classList.add("show");
  }, 10);

  // Rimuove la notifica dopo 3 secondi
  setTimeout(() => {
    notification.classList.remove("show");

    // Rimuove l'elemento dal DOM dopo che l'animazione è completata
    setTimeout(() => {
      notification.remove();
    }, 300);
  }, 3000);
}
