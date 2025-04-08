document.addEventListener("DOMContentLoaded", function () {
  console.log("Initializing cart page functionality");

  // Inizializza i pulsanti di modifica quantità
  initQuantityButtons();

  // Inizializza i pulsanti di rimozione elementi
  initRemoveButtons();

  // Inizializza eventuali pulsanti "Aggiungi al carrello" nei prodotti consigliati
  initAddToCartButtons();
});

/**
 * Inizializza i pulsanti per modificare le quantità nel carrello
 */
function initQuantityButtons() {
  const decreaseButtons = document.querySelectorAll(".quantity-decrease");
  const increaseButtons = document.querySelectorAll(".quantity-increase");

  console.log(
    `Found ${decreaseButtons.length} decrease buttons and ${increaseButtons.length} increase buttons`
  );

  decreaseButtons.forEach((button) => {
    button.addEventListener("click", function (event) {
      event.preventDefault();
      console.log("Decrease button clicked");

      const cartItemId = this.getAttribute("data-item-id");
      if (!cartItemId) {
        console.error("Missing item ID on decrease button");
        return;
      }

      const quantityElement = document.getElementById(
        `item-quantity-${cartItemId}`
      );
      if (!quantityElement) {
        console.error(`Quantity element not found for item ${cartItemId}`);
        return;
      }

      let currentQuantity = parseInt(quantityElement.textContent);

      if (currentQuantity > 1) {
        updateCartItemQuantity(cartItemId, currentQuantity - 1);
      } else {
        showConfirmationModal(
          "Rimuovi prodotto",
          "Vuoi rimuovere questo prodotto dal carrello?",
          () => removeCartItem(cartItemId)
        );
      }
    });
  });

  increaseButtons.forEach((button) => {
    button.addEventListener("click", function (event) {
      event.preventDefault();
      console.log("Increase button clicked");

      const cartItemId = this.getAttribute("data-item-id");
      if (!cartItemId) {
        console.error("Missing item ID on increase button");
        return;
      }

      const quantityElement = document.getElementById(
        `item-quantity-${cartItemId}`
      );
      if (!quantityElement) {
        console.error(`Quantity element not found for item ${cartItemId}`);
        return;
      }

      let currentQuantity = parseInt(quantityElement.textContent);
      updateCartItemQuantity(cartItemId, currentQuantity + 1);
    });
  });
}

/**
 * Inizializza i pulsanti per rimuovere elementi dal carrello
 */
function initRemoveButtons() {
  console.log("Initializing remove buttons");
  const removeButtons = document.querySelectorAll(".remove-item-btn");
  console.log(`Found ${removeButtons.length} remove buttons`);

  removeButtons.forEach((button) => {
    // Rimuovi eventuali event listener esistenti (per sicurezza)
    const newButton = button.cloneNode(true);
    button.parentNode.replaceChild(newButton, button);

    // Aggiungi il nuovo event listener
    newButton.addEventListener("click", function (event) {
      event.preventDefault();
      event.stopPropagation();

      const cartItemId = this.getAttribute("data-item-id");
      console.log(`Remove button clicked for item ID: ${cartItemId}`);

      if (!cartItemId) {
        console.error("Missing item ID on remove button");
        return;
      }

      // Test diretto - prova a rimuovere senza modale per debug
      console.log("Calling removeCartItem directly");
      removeCartItem(cartItemId);
    });
  });
}

/**
 * Inizializza i pulsanti "Aggiungi al carrello" nei prodotti consigliati
 */
function initAddToCartButtons() {
  const addToCartButtons = document.querySelectorAll(".add-to-cart-btn");
  console.log(`Found ${addToCartButtons.length} add to cart buttons`);

  addToCartButtons.forEach((button) => {
    button.addEventListener("click", function (event) {
      event.preventDefault();

      const productId = this.getAttribute("data-product-id");
      if (!productId) {
        console.error("Missing product ID on add-to-cart button");
        return;
      }

      console.log(`Add to cart clicked for product ID: ${productId}`);

      let quantity = 1;
      // Se esiste un campo di input per la quantità, usare quel valore
      const quantityInput =
        this.closest(".product-actions")?.querySelector(".quantity-input");
      if (quantityInput) {
        quantity = parseInt(quantityInput.value) || 1;
      }

      addProductToCart(productId, quantity);
    });
  });
}

/**
 * Ottiene il token CSRF dalla pagina
 * @returns {Object} Token e header CSRF
 */
function getCsrfInfo() {
  const token = document
    .querySelector('meta[name="_csrf"]')
    ?.getAttribute("content");
  const header = document
    .querySelector('meta[name="_csrf_header"]')
    ?.getAttribute("content");

  if (!token || !header) {
    console.error("CSRF token o header non disponibili");
    return { token: null, header: null };
  }

  return { token, header };
}

/**
 * Aggiorna la quantità di un elemento nel carrello
 * @param {string} cartItemId - ID dell'elemento nel carrello
 * @param {number} quantity - Nuova quantità
 */
function updateCartItemQuantity(cartItemId, quantity) {
  console.log(`Updating quantity for item ${cartItemId} to ${quantity}`);

  const { token, header } = getCsrfInfo();
  if (!token || !header) {
    showCartNotification("Errore di sicurezza: token CSRF mancante", "error");
    return;
  }

  // Disabilita temporaneamente i pulsanti correlati
  const decreaseBtn = document.querySelector(
    `.quantity-decrease[data-item-id="${cartItemId}"]`
  );
  const increaseBtn = document.querySelector(
    `.quantity-increase[data-item-id="${cartItemId}"]`
  );

  if (decreaseBtn) decreaseBtn.disabled = true;
  if (increaseBtn) increaseBtn.disabled = true;

  const formData = new FormData();
  formData.append("quantity", quantity);

  fetch(`/cart/update/${cartItemId}`, {
    method: "PUT",
    headers: {
      [header]: token,
    },
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
      console.log("Update quantity response:", data);

      if (data.success) {
        // Aggiorna l'interfaccia utente
        updateCartUI(cartItemId, quantity, data);
      } else {
        showCartNotification(
          data.message || "Errore durante l'aggiornamento",
          "error"
        );
      }
    })
    .catch((error) => {
      console.error("Error updating quantity:", error);
      showCartNotification(
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
function removeCartItem(cartItemId) {
  console.log(`Removing item ${cartItemId} from cart`);

  const { token, header } = getCsrfInfo();
  if (!token || !header) {
    showCartNotification("Errore di sicurezza: token CSRF mancante", "error");
    return;
  }

  // Disabilita temporaneamente il pulsante rimuovi
  const removeBtn = document.querySelector(
    `.remove-item-btn[data-item-id="${cartItemId}"]`
  );
  if (removeBtn) removeBtn.disabled = true;

  fetch(`/cart/remove/${cartItemId}`, {
    method: "DELETE",
    headers: {
      [header]: token,
    },
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
      console.log("Remove item response:", data);

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
        showCartNotification(
          data.message || "Prodotto rimosso con successo",
          "success"
        );

        // Se il carrello è vuoto, aggiorna l'interfaccia
        if (data.cartItemCount === 0) {
          showEmptyCartMessage();
        }
      } else {
        showCartNotification(
          data.message || "Errore durante la rimozione",
          "error"
        );
      }
    })
    .catch((error) => {
      console.error("Error removing item:", error);
      showCartNotification(
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
 * Aggiunge un prodotto al carrello
 * @param {string} productId - ID del prodotto
 * @param {number} quantity - Quantità da aggiungere
 */
function addProductToCart(productId, quantity) {
  console.log(`Adding product ${productId} to cart with quantity ${quantity}`);

  const { token, header } = getCsrfInfo();
  if (!token || !header) {
    showCartNotification("Errore di sicurezza: token CSRF mancante", "error");
    return;
  }

  // Disabilita temporaneamente i pulsanti correlati
  const addToCartBtn = document.querySelector(
    `.add-to-cart-btn[data-product-id="${productId}"]`
  );
  if (addToCartBtn) addToCartBtn.disabled = true;

  const formData = new FormData();
  formData.append("quantity", quantity);

  fetch(`/cart/add/${productId}`, {
    method: "POST",
    headers: {
      [header]: token,
    },
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
      console.log("Add to cart response:", data);

      if (data.success) {
        // Aggiorna il contatore del carrello
        updateCartCounter(data.cartItemCount);

        // Mostra un messaggio di notifica
        showCartNotification(
          data.message || "Prodotto aggiunto al carrello",
          "success"
        );
      } else {
        showCartNotification(
          data.message || "Errore durante l'aggiunta",
          "error"
        );
      }
    })
    .catch((error) => {
      console.error("Error adding to cart:", error);
      showCartNotification(
        "Si è verificato un errore durante l'aggiunta al carrello",
        "error"
      );
    })
    .finally(() => {
      // Riattiva i pulsanti
      if (addToCartBtn) addToCartBtn.disabled = false;
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
    console.error("Error updating cart UI:", error);
  }
}

/**
 * Aggiorna il riepilogo del carrello
 * @param {Object} data - Dati dal server
 */
function updateCartSummary(data) {
  try {
    if (!data) return;

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
    console.error("Error updating cart summary:", error);
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
 * @param {string} type - Tipo di notifica (success, error, info)
 */
function showCartNotification(message, type = "info") {
  // Rimuove eventuali notifiche esistenti
  const existingNotifications = document.querySelectorAll(".cart-notification");
  existingNotifications.forEach((notification) => {
    if (notification.parentNode) {
      notification.parentNode.removeChild(notification);
    }
  });

  // Crea l'elemento di notifica
  const notification = document.createElement("div");
  notification.className = `cart-notification cart-notification-${type}`;
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

// Aggiungi stili per la modale e le notifiche
const cartStyles = `
<style>
.confirmation-modal .modal-dialog {
    max-width: 500px;
    margin: 100px auto;
}

.cart-notification {
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

.cart-notification-success { 
    background-color: #28a745; 
}

.cart-notification-error { 
    background-color: #dc3545; 
}

.cart-notification-info { 
    background-color: #17a2b8; 
}

.cart-notification.show {
    opacity: 1;
}

button:disabled {
    cursor: not-allowed;
    opacity: 0.6;
}
</style>
`;

// Aggiungi gli stili al documento
document.head.insertAdjacentHTML("beforeend", cartStyles);
