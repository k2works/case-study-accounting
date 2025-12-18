describe('Sample E2E Test', () => {
  it('should load the application', () => {
    cy.visit('/');
    cy.contains('財務会計システム').should('be.visible');
  });
});
