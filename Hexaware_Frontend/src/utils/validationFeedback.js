import { useEffect } from 'react';

const FEEDBACK_CLASS = 'careassist-field-error';
const SUBMITTING_CLASS = 'careassist-form-submitted';

function getFieldLabel(field) {
  const explicit = field.getAttribute('aria-label') || field.getAttribute('placeholder');
  if (explicit) return explicit.trim();

  const id = field.getAttribute('id');
  if (id) {
    const label = document.querySelector(`label[for="${CSS.escape(id)}"]`);
    if (label?.textContent) return label.textContent.trim();
  }

  const parentLabel = field.closest('.mb-3, .form-group, .col-md-6, .col-lg-6, .col-lg-4, .col-lg-7, .col-lg-5')?.querySelector('label');
  if (parentLabel?.textContent) return parentLabel.textContent.trim();

  const name = field.getAttribute('name') || 'This field';
  return name
    .replace(/([A-Z])/g, ' $1')
    .replace(/[_-]+/g, ' ')
    .replace(/^./, (char) => char.toUpperCase())
    .trim();
}

function getValidationMessage(field) {
  const label = getFieldLabel(field);
  const validity = field.validity;

  if (field.dataset.serverError) return field.dataset.serverError;
  if (field.validationMessage && !validity.valid && !validity.customError) {
    if (validity.valueMissing) return `${label} is required.`;
    if (validity.typeMismatch && field.type === 'email') return 'Enter a valid email address.';
    if (validity.patternMismatch) return field.getAttribute('title') || `${label} format is invalid.`;
    if (validity.tooShort) return `${label} must be at least ${field.minLength} characters.`;
    if (validity.tooLong) return `${label} must be at most ${field.maxLength} characters.`;
    if (validity.rangeUnderflow) return `${label} must be at least ${field.min}.`;
    if (validity.rangeOverflow) return `${label} must be at most ${field.max}.`;
    if (validity.stepMismatch) return `${label} has an invalid number value.`;
    if (validity.badInput) return `${label} has an invalid value.`;
  }

  return field.validationMessage || `${label} is invalid.`;
}

function getFeedbackElement(field) {
  const container = field.parentElement || field.closest('.mb-3');
  let feedback = field.nextElementSibling;

  if (!feedback?.classList?.contains(FEEDBACK_CLASS)) {
    feedback = document.createElement('div');
    feedback.className = `invalid-feedback ${FEEDBACK_CLASS}`;
    field.insertAdjacentElement('afterend', feedback);
  }

  if (container && !container.classList.contains('position-relative')) {
    container.classList.add('position-relative');
  }

  return feedback;
}

function clearFieldError(field) {
  if (!field?.matches?.('input, select, textarea')) return;

  field.classList.remove('is-invalid');
  delete field.dataset.serverError;

  if (!field.dataset.customValidation) {
    field.setCustomValidity('');
  }

  const feedback = field.nextElementSibling;
  if (feedback?.classList?.contains(FEEDBACK_CLASS)) {
    feedback.textContent = '';
  }
}

function showFieldError(field) {
  if (!field?.matches?.('input, select, textarea')) return;
  if (field.disabled || field.type === 'hidden') return;

  field.classList.add('is-invalid');
  const feedback = getFeedbackElement(field);
  feedback.textContent = getValidationMessage(field);
}

function getFormControls(form) {
  return Array.from(form.querySelectorAll('input, select, textarea')).filter(
    (field) => !field.disabled && field.type !== 'hidden' && field.type !== 'button' && field.type !== 'submit',
  );
}

function applyPasswordMatchValidation(form) {
  const password = form.querySelector('input[name="password"], input[name="newPassword"]');
  const confirmPassword = form.querySelector('input[name="confirmPassword"]');

  if (!password || !confirmPassword) return;

  const mismatch = Boolean(password.value && confirmPassword.value && password.value !== confirmPassword.value);
  confirmPassword.dataset.customValidation = 'true';
  confirmPassword.setCustomValidity(mismatch ? 'Password and confirm password must match.' : '');

  if (!mismatch) {
    delete confirmPassword.dataset.customValidation;
  }
}

function validateForm(form) {
  applyPasswordMatchValidation(form);

  let firstInvalid = null;
  getFormControls(form).forEach((field) => {
    if (field.checkValidity()) {
      clearFieldError(field);
    } else {
      showFieldError(field);
      firstInvalid ||= field;
    }
  });

  if (firstInvalid) {
    firstInvalid.focus({ preventScroll: true });
    firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  return !firstInvalid;
}

function applyServerFieldErrors(fieldErrors) {
  const form = window.__careassistLastSubmittedForm || document.querySelector(`form.${SUBMITTING_CLASS}`) || document.querySelector('form');
  if (!form || !fieldErrors || typeof fieldErrors !== 'object') return;

  let firstInvalid = null;

  Object.entries(fieldErrors).forEach(([fieldName, message]) => {
    const field = form.querySelector(`[name="${CSS.escape(fieldName)}"]`);
    if (!field) return;

    field.dataset.serverError = Array.isArray(message) ? message.join(', ') : String(message);
    field.setCustomValidity(field.dataset.serverError);
    showFieldError(field);
    firstInvalid ||= field;
  });

  if (firstInvalid) {
    firstInvalid.focus({ preventScroll: true });
    firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }
}

export function useValidationFeedback() {
  useEffect(() => {
    const markFormsNoValidate = () => {
      Array.from(document.querySelectorAll('form')).forEach((form) =>
        form.setAttribute('novalidate', 'novalidate'),
      );
    };

    markFormsNoValidate();

    const observer = new MutationObserver(markFormsNoValidate);
    observer.observe(document.body, { childList: true, subtree: true });

    const handleSubmit = (event) => {
      const form = event.target;
      if (!(form instanceof HTMLFormElement)) return;

      form.classList.add(SUBMITTING_CLASS);
      window.__careassistLastSubmittedForm = form;

      if (!validateForm(form)) {
        event.preventDefault();
        event.stopPropagation();
        event.stopImmediatePropagation?.();
      }
    };

    const handleInput = (event) => {
      const field = event.target;
      if (!field?.matches?.('input, select, textarea')) return;
      const form = field.form;
      if (!form) return;

      delete field.dataset.serverError;
      field.setCustomValidity('');
      applyPasswordMatchValidation(form);

      if (form.classList.contains(SUBMITTING_CLASS) || field.classList.contains('is-invalid')) {
        if (field.checkValidity()) clearFieldError(field);
        else showFieldError(field);
      }

      const confirmPassword = form.querySelector('input[name="confirmPassword"]');
      if (confirmPassword?.classList.contains('is-invalid')) {
        if (confirmPassword.checkValidity()) clearFieldError(confirmPassword);
        else showFieldError(confirmPassword);
      }
    };

    const handleServerValidation = (event) => {
      applyServerFieldErrors(event.detail?.fieldErrors);
    };

    document.addEventListener('submit', handleSubmit, true);
    document.addEventListener('input', handleInput, true);
    document.addEventListener('change', handleInput, true);
    window.addEventListener('careassist:server-validation', handleServerValidation);

    return () => {
      document.removeEventListener('submit', handleSubmit, true);
      document.removeEventListener('input', handleInput, true);
      document.removeEventListener('change', handleInput, true);
      window.removeEventListener('careassist:server-validation', handleServerValidation);
      observer.disconnect();
    };
  }, []);
}
