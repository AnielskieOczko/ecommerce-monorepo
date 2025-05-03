import React, { Component, ErrorInfo, ReactNode } from 'react';
import { errors} from '../utils/errorHandling';

interface Props {
    children: ReactNode;
    fallback?: ReactNode;
}

interface State {
    hasError: boolean;
    error: Error | null;
}

export class ErrorBoundary extends Component<Props, State> {
    public state: State = {
        hasError: false,
        error: null
    };

    public static getDerivedStateFromError(error: Error): State {
        return { hasError: true, error };
    }

    public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
        console.error('Uncaught error:', error, errorInfo);
    }

    private renderErrorMessage() {
        const { error } = this.state;

        if (error instanceof errors.api) {
            return (
                <div className="bg-red-50 p-4 rounded-md">
                    <h3 className="text-red-800">API Error</h3>
                    <p className="text-red-600">{error.message}</p>
                    {error.name && (
                        <pre className="mt-2 text-sm text-red-500">
                            {JSON.stringify(error.message, null, 2)}
                        </pre>
                    )}
                </div>
            );
        }

        if (error instanceof errors.network) {
            return (
                <div className="bg-yellow-50 p-4 rounded-md">
                    <h3 className="text-yellow-800">Network Error</h3>
                    <p className="text-yellow-600">
                        Please check your internet connection and try again.
                    </p>
                </div>
            );
        }

        if (error instanceof errors.auth) {
            return (
                <div className="bg-orange-50 p-4 rounded-md">
                    <h3 className="text-orange-800">Authentication Error</h3>
                    <p className="text-orange-600">
                        Please log in again to continue.
                    </p>
                </div>
            );
        }

        return (
            <div className="bg-red-50 p-4 rounded-md">
                <h3 className="text-red-800">Unexpected Error</h3>
                <p className="text-red-600">{error?.message || 'An unknown error occurred'}</p>
            </div>
        );
    }

    public render() {
        if (this.state.hasError) {
            return this.props.fallback || this.renderErrorMessage();
        }

        return this.props.children;
    }
} 