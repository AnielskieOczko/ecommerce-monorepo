import api from './api';

const BASE_URL = '/api/v1/admin/authorities';

export const authorityService = {
  getAuthorityNames: async (): Promise<Set<string>> => {
    const response = await api.get<Set<string>>(`${BASE_URL}/names`);
    return response.data;
  },
};

export default authorityService;
