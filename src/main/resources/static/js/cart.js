// cart.js - Soluzione completa per la funzionalità del carrello
// Compatibile con cart.html, products/list.html e products/detail.html

// Configura il logging
const DEBUG = false; // Impostare a false in produzione
function log(message, data) {
  if (DEBUG) {
    if (data) {
      console.log(`[Cart] ${message}`, data);
    } else {
      console.log(`[Cart] ${message}`);
    }
  }
}

log("Initializing cart.js...");

// Globale per lo stato del carrello
const cartState = {
  initialized: false,
};

/**
 * Inizializza tutti gli handler per gli eventi del carrello
 * Questa funzione viene chiamata automaticamente alla fine di questo file
 */
function initializeCartHandlers() {
  if (cartState.initialized) {
    log("Cart handlers already initialized, skipping");
    return;
  }

  log("Setting up global cart event handlers");

  // IMPORTANTE: Non sovrascriviamo gli event listener esistenti nelle pagine detail.html e list.html
  // Ci concentriamo solo su cart.html dove i pulsanti non funzionavano

  // Inizializza solo i pulsanti di quanità e rimozione nel carrello
  initQuantityButtons();
  initRemoveButtons();

  // Imposta flag di inizializzazione
  cartState.initialized = true;
  log("Cart handlers initialized successfully");
}

/**
 * Inizializza i pulsanti per modificare le quantità nel carrello
 */
function initQuantityButtons() {
  const decreaseButtons = document.querySelectorAll(".quantity-decrease");
  const increaseButtons = document.querySelectorAll(".quantity-increase");

  log(
    `Found ${decreaseButtons.length} decrease buttons and ${increaseButtons.length} increase buttons`
  );

  decreaseButtons.forEach((button) => {
    button.addEventListener("click", function (event) {
      event.preventDefault();
      log("Decrease button clicked");

      const cartItemId = this.getAttribute("data-item-id");
      if (!cartItemId) {
        log("Missing item ID on decrease button");
        return;
      }

      const quantityElement = document.getElementById(
        `item-quantity-${cartItemId}`
      );
      if (!quantityElement) {
        log(`Quantity element not found for item ${cartItemId}`);
        return;
      }

      let currentQuantity = parseInt(quantityElement.textContent);

      if (currentQuantity > 1) {
        updateQuantity(cartItemId, currentQuantity - 1);
      } else {
        showConfirmationModal(
          "Rimuovi prodotto",
          "Vuoi rimuovere questo prodotto dal carrello?",
          () => removeFromCart(cartItemId)
        );
      }
    });
  });

  increaseButtons.forEach((button) => {
    button.addEventListener("click", function (event) {
      event.preventDefault();
      log("Increase button clicked");

      const cartItemId = this.getAttribute("data-item-id");
      if (!cartItemId) {
        log("Missing item ID on increase button");
        return;
      }

      const quantityElement = document.getElementById(
        `item-quantity-${cartItemId}`
      );
      if (!quantityElement) {
        log(`Quantity element not found for item ${cartItemId}`);
        return;
      }

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
  log(`Found ${removeButtons.length} remove buttons`);

  removeButtons.forEach((button) => {
    button.addEventListener("click", function (event) {
      event.preventDefault();
      log("Remove button clicked");

      const cartItemId = this.getAttribute("data-item-id");
      if (!cartItemId) {
        log("Missing item ID on remove button");
        return;
      }

      showConfirmationModal(
        "Rimuovi prodotto",
        "Sei sicuro di voler rimuovere questo prodotto dal carrello?",
        () => removeFromCart(cartItemId)
      );
    });
  });
}

/**
 * Ottiene il token CSRF e l'header in modo affidabile
 * @returns {Object} Oggetto con token e header CSRF
 */
function getCsrfData() {
  let token = null;
  let header = null;

  // Prova a ottenere dal meta tag
  const metaToken = document.querySelector('meta[name="_csrf"]');
  const metaHeader = document.querySelector('meta[name="_csrf_header"]');

  if (metaToken && metaHeader) {
    token = metaToken.getAttribute("content");
    header = metaHeader.getAttribute("content");
    log("CSRF token found in meta tags");
  } else {
    // Fallback: prova a ottenere token da input nascosto
    const inputToken = document.querySelector('input[name="_csrf"]');
    if (inputToken) {
      token = inputToken.value;
      // Usa X-CSRF-TOKEN come header predefinito quando non è definito esplicitamente
      header = "X-CSRF-TOKEN";
      log("CSRF token found in hidden input field");
    } else {
      log("WARNING: CSRF token not found in document");
    }
  }

  return { token, header };
}

/**
 * Prepara headers per una richiesta AJAX con CSRF token
 * @returns {Object} Headers per la richiesta fetch
 */
function prepareHeaders() {
  const { token, header } = getCsrfData();
  const headers = {};

  if (token && header) {
    headers[header] = token;
  }

  return headers;
}

/**
 * Aggiunge un prodotto al carrello
 * @param {string} productId - ID del prodotto
 * @param {number} quantity - Quantità da aggiungere
 * @returns {Promise} Promise che risolve con la risposta del server
 */
function addToCart(productId, quantity) {
  log(`Adding product ${productId} to cart with quantity ${quantity}`);

  const headers = prepareHeaders();
  const formData = new FormData();
  formData.append("quantity", quantity);

  // Bottoni che potrebbero essere presenti nelle diverse pagine
  toggleLoadingState(true, `.add-to-cart-btn[data-product-id="${productId}"]`);
  toggleLoadingState(true, `#addToCartBtn`);

  return new Promise((resolve, reject) => {
    fetch(`/cart/add/${productId}`, {
      method: "POST",
      headers: headers,
      body: formData,
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(
            `Server returned ${response.status}: ${response.statusText}`
          );
        }
        return response.json();
      })
      .then((data) => {
        log("Add to cart response:", data);

        if (data.success) {
          // Aggiorna il contatore del carrello
          updateCartCounter(data.cartItemCount);

          // Mostra un messaggio di notifica
          showNotification(
            data.message || "Prodotto aggiunto al carrello",
            "success"
          );
        } else {
          showNotification(
            data.message || "Errore durante l'aggiunta",
            "error"
          );

          // Se l'utente non è autenticato, reindirizza al login
          if (data.message && data.message.includes("login")) {
            setTimeout(() => {
              window.location.href = "/login";
            }, 2000);
          }
        }

        // Risolvi la Promise con i dati dal server
        resolve(data);
      })
      .catch((error) => {
        log("Error adding to cart:", error);
        showNotification(
          "Si è verificato un errore durante l'aggiunta al carrello",
          "error"
        );

        // Rifiuta la Promise con l'errore
        reject(error);
      })
      .finally(() => {
        // Riattiva i pulsanti
        toggleLoadingState(
          false,
          `.add-to-cart-btn[data-product-id="${productId}"]`
        );
        toggleLoadingState(false, `#addToCartBtn`);
      });
  });
}

/**
 * Aggiorna la quantità di un elemento nel carrello
 * @param {string} cartItemId - ID dell'elemento nel carrello
 * @param {number} quantity - Nuova quantità
 */
function updateQuantity(cartItemId, quantity) {
  log(`Updating quantity for item ${cartItemId} to ${quantity}`);

  const headers = prepareHeaders();
  const formData = new FormData();
  formData.append("quantity", quantity);

  // Disabilita temporaneamente i pulsanti correlati
  const decreaseBtn = document.querySelector(
    `.quantity-decrease[data-item-id="${cartItemId}"]`
  );
  const increaseBtn = document.querySelector(
    `.quantity-increase[data-item-id="${cartItemId}"]`
  );

  if (decreaseBtn) decreaseBtn.disabled = true;
  if (increaseBtn) increaseBtn.disabled = true;

  fetch(`/cart/update/${cartItemId}`, {
    method: "PUT",
    headers: headers,
    body: formData,
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(
          `Server returned ${response.status}: ${response.statusText}`
        );
      }
      return response.json();
    })
    .then((data) => {
      log("Update quantity response:", data);

      if (data.success) {
        // Aggiorna l'interfaccia utente
        updateCartUI(cartItemId, quantity, data);
      } else {
        showNotification(
          data.message || "Errore durante l'aggiornamento",
          "error"
        );
      }
    })
    .catch((error) => {
      log("Error updating quantity:", error);
      showNotification(
        "Si è verificato un errore durante l'aggiornamento della quantità",
        "error"
      );
    })
    .finally(() => {
      // Riattiva i pulsanti
      if (decreaseBtn) decreaseBtn.disabled = false;
      if (increaseBtn) increaseBtn.disabled = false;
    });
}

/**
 * Rimuove un elemento dal carrello
 * @param {string} cartItemId - ID dell'elemento nel carrello
 */
function removeFromCart(cartItemId) {
  log(`Removing item ${cartItemId} from cart`);

  const headers = prepareHeaders();

  // Disabilita temporaneamente il pulsante rimuovi
  const removeBtn = document.querySelector(
    `.remove-item-btn[data-item-id="${cartItemId}"]`
  );
  if (removeBtn) removeBtn.disabled = true;

  fetch(`/cart/remove/${cartItemId}`, {
    method: "DELETE",
    headers: headers,
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(
          `Server returned ${response.status}: ${response.statusText}`
        );
      }
      return response.json();
    })
    .then((data) => {
      log("Remove item response:", data);

      if (data.success) {
        // Rimuove l'elemento dalla pagina
        const cartItemElement = document.getElementById(
          `cart-item-${cartItemId}`
        );
        if (cartItemElement) {
          cartItemElement.remove();
        }

        // Aggiorna il resto dell'interfaccia
        updateCartSummary(data);

        // Mostra un messaggio di notifica
        showNotification(
          data.message || "Prodotto rimosso con successo",
          "success"
        );

        // Se il carrello è vuoto, aggiorna l'interfaccia
        if (data.cartItemCount === 0) {
          showEmptyCartMessage();
        }
      } else {
        showNotification(
          data.message || "Errore durante la rimozione",
          "error"
        );
      }
    })
    .catch((error) => {
      log("Error removing item:", error);
      showNotification(
        "Si è verificato un errore durante la rimozione dal carrello",
        "error"
      );
    })
    .finally(() => {
      // Riattiva il pulsante se esiste ancora
      if (removeBtn) removeBtn.disabled = false;
    });
}

/**
 * Aggiorna l'interfaccia utente del carrello dopo l'aggiornamento della quantità
 * @param {string} cartItemId - ID dell'elemento nel carrello
 * @param {number} quantity - Nuova quantità
 * @param {Object} data - Dati dal server
 */
function updateCartUI(cartItemId, quantity, data) {
  try {
    // Aggiorna la quantità visualizzata
    const quantityElement = document.getElementById(
      `item-quantity-${cartItemId}`
    );
    if (quantityElement) {
      quantityElement.textContent = quantity;
    }

    // Aggiorna il totale dell'elemento
    const itemTotalElement = document.getElementById(
      `item-total-${cartItemId}`
    );
    if (itemTotalElement && data.itemTotal !== undefined) {
      itemTotalElement.textContent = formatCurrency(data.itemTotal);
    }

    // Aggiorna il resto dell'interfaccia
    updateCartSummary(data);
  } catch (error) {
    log("Error updating cart UI:", error);
  }
}

/**
 * Aggiorna il riepilogo del carrello
 * @param {Object} data - Dati dal server
 */
function updateCartSummary(data) {
  try {
    // Aggiorna il subtotale
    const cartTotalElement = document.getElementById("cart-total");
    if (cartTotalElement && data.cartTotal !== undefined) {
      cartTotalElement.textContent = formatCurrency(data.cartTotal);
    }

    const cartTotal = data.cartTotal || 0;

    // Calcola i costi di spedizione
    const shippingCost = cartTotal > 50 ? 0 : 4.99;
    const totalWithShipping = cartTotal + shippingCost;

    // Aggiorna il totale con spedizione
    const cartTotalAmountElement = document.querySelector(
      ".cart-total-amount span:last-child"
    );
    if (cartTotalAmountElement) {
      cartTotalAmountElement.textContent = formatCurrency(totalWithShipping);
    }

    // Aggiorna info spedizione
    const shippingElement = document.querySelector(
      ".cart-summary-item.cart-shipping span:last-child"
    );
    if (shippingElement) {
      shippingElement.textContent =
        cartTotal > 50 ? "Gratuita" : formatCurrency(4.99);
    }

    // Aggiorna messaggio spedizione
    const shippingInfoElement = document.querySelector(".shipping-info");
    if (shippingInfoElement) {
      if (cartTotal > 50) {
        shippingInfoElement.innerHTML = `
          <p class="free-shipping-message">
            <i class="fas fa-check-circle"></i> Spedizione gratuita applicata
          </p>
        `;
      } else {
        shippingInfoElement.innerHTML = `
          <p class="shipping-threshold-message">
            <i class="fas fa-info-circle"></i> Aggiungi 
            <span>${formatCurrency(50 - cartTotal)}</span> 
            per ottenere la spedizione gratuita
          </p>
        `;
      }
    }

    // Aggiorna il contatore del carrello
    if (data.cartItemCount !== undefined) {
      updateCartCounter(data.cartItemCount);
    }
  } catch (error) {
    log("Error updating cart summary:", error);
  }
}

/**
 * Mostra il messaggio di carrello vuoto
 */
function showEmptyCartMessage() {
  const cartContent = document.querySelector(".cart-content");

  if (cartContent) {
    cartContent.innerHTML = `
      <div class="empty-cart text-center">
        <div class="empty-cart-icon mb-4">
          <i class="fas fa-shopping-cart fa-4x text-muted"></i>
        </div>
        <h2 class="mb-3">Il tuo carrello è vuoto</h2>
        <p class="lead mb-4">Aggiungi alcuni prodotti al carrello per procedere con l'acquisto.</p>
        <a href="/products" class="btn btn-primary btn-lg">
          <i class="fas fa-arrow-left me-2"></i> Continua lo shopping
        </a>
      </div>
    `;
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
 * Aggiorna il contatore del carrello nell'header
 * @param {number} count - Numero di elementi nel carrello
 */
function updateCartCounter(count) {
  const cartCounter = document.querySelector(".cart-counter");

  if (cartCounter) {
    cartCounter.textContent = count;

    // Mostra/nascondi il contatore
    if (count === 0) {
      cartCounter.style.display = "none";
    } else {
      cartCounter.style.display = "flex";
    }
  }
}

/**
 * Mostra una notifica all'utente
 * @param {string} message - Messaggio da mostrare
 * @param {string} type - Tipo di notifica (success, error, info, warning, danger)
 */
function showNotification(message, type = "info") {
  // Mappa i tipi di notifica della pagina di prodotto a quelli usati nel carrello
  if (type === "warning") type = "error";
  if (type === "danger") type = "error";

  // Rimuove eventuali notifiche esistenti
  const existingNotifications = document.querySelectorAll(".notification");
  existingNotifications.forEach((notification) => {
    if (notification.parentNode) {
      notification.parentNode.removeChild(notification);
    }
  });

  // Crea l'elemento di notifica
  const notification = document.createElement("div");
  notification.className = `notification notification-${type}`;
  notification.textContent = message;

  // Aggiunge la notifica al DOM
  document.body.appendChild(notification);

  // Mostra la notifica
  setTimeout(() => {
    notification.classList.add("show");
  }, 10);

  // Rimuove la notifica
  setTimeout(() => {
    notification.classList.remove("show");

    // Rimuove l'elemento dal DOM
    setTimeout(() => {
      if (notification.parentNode) {
        notification.parentNode.removeChild(notification);
      }
    }, 300);
  }, 3000);
}

/**
 * Mostra una modale di conferma
 * @param {string} title - Titolo della modale
 * @param {string} message - Messaggio di conferma
 * @param {Function} onConfirm - Funzione da eseguire se confermato
 */
function showConfirmationModal(title, message, onConfirm) {
  // Rimuove eventuali modali esistenti
  const existingModal = document.querySelector(".confirmation-modal");
  if (existingModal && existingModal.parentNode) {
    existingModal.parentNode.removeChild(existingModal);
  }

  // Crea la modale
  const modalHtml = `
    <div class="modal confirmation-modal" tabindex="-1" role="dialog" style="display:block; background-color: rgba(0,0,0,0.5); position: fixed; top: 0; left: 0; width: 100%; height: 100%; z-index: 1050;">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">${title}</h5>
            <button type="button" class="btn-close cancel-modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <p>${message}</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary cancel-modal">Annulla</button>
            <button type="button" class="btn btn-danger confirm-modal">Conferma</button>
          </div>
        </div>
      </div>
    </div>
  `;

  // Aggiunge la modale al body
  const modalContainer = document.createElement("div");
  modalContainer.innerHTML = modalHtml;
  document.body.appendChild(modalContainer.firstChild);

  // Gestisce la chiusura della modale
  const modal = document.querySelector(".confirmation-modal");
  const cancelButtons = modal.querySelectorAll(".cancel-modal");
  const confirmButton = modal.querySelector(".confirm-modal");

  // Funzione per rimuovere la modale
  function removeModal() {
    if (modal && modal.parentNode) {
      modal.parentNode.removeChild(modal);
    }
  }

  // Evento per annullare
  cancelButtons.forEach((button) => {
    button.addEventListener("click", removeModal);
  });

  // Evento per confermare
  confirmButton.addEventListener("click", () => {
    removeModal();
    onConfirm();
  });
}

/**
 * Attiva/disattiva lo stato di caricamento per un elemento
 * @param {boolean} isLoading - Se true, attiva lo stato di caricamento
 * @param {string} selector - Selettore CSS per gli elementi da modificare
 */
function toggleLoadingState(isLoading, selector) {
  const elements = document.querySelectorAll(selector);

  elements.forEach((element) => {
    if (isLoading) {
      element.setAttribute("disabled", "disabled");
      element.classList.add("loading");
    } else {
      element.removeAttribute("disabled");
      element.classList.remove("loading");
    }
  });
}

// Aggiungi stili per la modale e le notifiche
const styles = `
<style>
.confirmation-modal .modal-dialog {
  max-width: 500px;
  margin: 100px auto;
}

.notification {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 15px 20px;
  border-radius: 4px;
  color: white;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 1060;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  max-width: 350px;
}

.notification-success { 
  background-color: #28a745; 
}

.notification-error { 
  background-color: #dc3545; 
}

.notification-warning { 
  background-color: #ffc107; 
  color: #212529; 
}

.notification-info { 
  background-color: #17a2b8; 
}

.notification.show {
  opacity: 1;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.loading {
  position: relative;
  color: transparent !important;
}

.loading::after {
  content: "";
  position: absolute;
  width: 16px;
  height: 16px;
  top: calc(50% - 8px);
  left: calc(50% - 8px);
  border: 2px solid rgba(255, 255, 255, 0.5);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 1s ease-in-out infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
`;

// Aggiungi gli stili al documento
document.head.insertAdjacentHTML("beforeend", styles);

// Inizializza gli handler quando il DOM è caricato
document.addEventListener("DOMContentLoaded", function () {
  log("DOM loaded, initializing cart handlers");
  initializeCartHandlers();
});

// Riesposizione di funzioni globali per garantire la compatibilità
window.addToCart = addToCart;
window.updateQuantity = updateQuantity;
window.removeFromCart = removeFromCart;
window.updateCartCounter = updateCartCounter;
window.showNotification = showNotification;
