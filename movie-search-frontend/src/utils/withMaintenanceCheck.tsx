import React from 'react';
import { useSelector } from 'react-redux';
import type { RootState } from '../store';
import { MaintenancePage } from '../components/feature';

/**
 * Higher-Order Component that automatically handles maintenance mode
 * Wraps any component and shows MaintenancePage if maintenance_mode is enabled
 */
export function withMaintenanceCheck<P extends object>(
  WrappedComponent: React.ComponentType<P>
) {
  const WithMaintenanceCheck: React.FC<P> = (props) => {
    // 直接从store中获取维护模式状态，不再主动获取feature flags
    const isMaintenanceMode = useSelector((state: RootState) => state.maintenance.isMaintenanceMode);

    if (isMaintenanceMode) {
      return <MaintenancePage />;
    }

    return <WrappedComponent {...props} />;
  };

  WithMaintenanceCheck.displayName = `withMaintenanceCheck(${WrappedComponent.displayName || WrappedComponent.name})`;

  return WithMaintenanceCheck;
}

export default withMaintenanceCheck;
